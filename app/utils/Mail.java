package utils;

import play.Configuration;
import play.Logger;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

import play.libs.Akka;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Send a mail with LabXplore.
 */
public class Mail {
    MailerClient mailerClient;

    public Mail(MailerClient mailerClient) {
        this.mailerClient = mailerClient;
    }


    /**
     * 1 second delay on sending emails
     */
    private static final int DELAY = 1;

    /**
     * Envelop to prepare.
     */
    public static class Envelop {
        public String subject;
        public String message;
        public List<String> toEmails;

        /**
         * Constructor of Envelop.
         *
         * @param subject  the subject
         * @param message  a message
         * @param toEmails list of emails adress
         */
        public Envelop(String subject, String message, List<String> toEmails) {
            this.subject = subject;
            this.message = message;
            this.toEmails = toEmails;
        }

        public Envelop(String subject, String message, String email) {
            this.message = message;
            this.subject = subject;
            this.toEmails = new ArrayList<String>();
            this.toEmails.add(email);
        }
    }

    /**
     * Send a email, using Akka to offload it to an actor.
     *
     * @param envelop envelop to send
     */
    public void sendMail(Mail.Envelop envelop) {
        EnvelopJob envelopJob = new EnvelopJob(envelop, mailerClient);
        final FiniteDuration delay = Duration.create(DELAY, TimeUnit.SECONDS);
        Akka.system().scheduler().scheduleOnce(delay, envelopJob, Akka.system().dispatcher());
    }

    static class EnvelopJob implements Runnable {
        MailerClient mailerClient;
        Mail.Envelop envelop;

        @Inject
        public EnvelopJob(Mail.Envelop envelop, MailerClient mailerClient) {
            this.envelop = envelop;
            this.mailerClient = mailerClient;
        }

        public void run() {
            Email email = new Email();

            final Configuration root = Configuration.root();
            final String mailFrom = root.getString("play.mailer.user");
            final String mailSign = root.getString("play.mailer.sign");

            email.setFrom(mailFrom);
            email.setSubject(envelop.subject);
            email.setBodyText(envelop.message + "\n\n " + mailSign);
            email.setBodyHtml(envelop.message + "<br><br>--<br>" + mailSign);
            for (String toEmail : envelop.toEmails) {
                email.addTo(toEmail);
                Logger.debug("Mail.sendMail: Mail will be sent to " + toEmail);
            }

            mailerClient.send(email);
            Logger.debug("Mail sent - " + root.getString("play.mailer.host")
                    + ":" + root.getString("play.mailer.port")
                    + " SSL:" + root.getString("play.mailer.ssl")
                    + " user:" + root.getString("play.mailer.user"));
        }
    }
}
