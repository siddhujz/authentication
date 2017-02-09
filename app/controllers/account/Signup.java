package controllers;


import models.User;

import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import javax.persistence.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.commons.mail.EmailException;

import utils.Hash;
import utils.Mail;

import static play.libs.Json.toJson;


public class Signup extends Controller { 
    private final FormFactory formFactory; 
    private final JPAApi jpaApi; 
    private final MailerClient mailerClient; 
    
    @Inject 
    public Signup(FormFactory formFactory, JPAApi jpaApi, MailerClient mailerClient) {
        this.formFactory = formFactory;
        this.jpaApi = jpaApi; 
        this.mailerClient = mailerClient;
        
    } 
    
    public static class Register { 
        @Constraints.Required
        public String email; 
        @Constraints.Required 
        public String fullname;
        @Constraints.Required 
        public String password;
        
        /** 
        * Validate the authentication. 
        * 
        * @return null if validation ok, string with details otherwise */ 
        
        public String validate() { 
            if (isBlank(email)) { 
                return "Email is required";
            } 
            if (isBlank(fullname)) { 
                return "Full Name is required"; 
            } if (isBlank(password)) { 
                return "Password is required"; 
            }
            return null;
        } 
        
        private boolean isBlank(String input) { 
            return input == null || input.isEmpty() || input.trim().isEmpty();
        } 
        
    } 
    
    public Result getRegister() { 
        Form<Register> registerForm = formFactory.form(Register.class); 
        return ok(views.html.register.render(registerForm)); 
    }
    
    @Transactional public Result doRegister() {
        Form<Register> registerForm = formFactory.form(Register.class).bindFromRequest(); 
        if (registerForm.hasErrors()) { 
            return badRequest(views.html.register.render(registerForm));
        } 
        
        Register register = registerForm.get();
        Result resultError = checkBeforeSave(registerForm, register.email);

        if (resultError != null) {
            return resultError;
       } else { 
            try { 
                User user = new User(); 
                user.email = register.email; 
                user.fullname = register.fullname; 
                user.passwordHash = Hash.createPassword(register.password); 
                user.confirmationToken = UUID.randomUUID().toString();
                jpaApi.em().persist(user); 
                
                //Send email to user asking for confirmation of account 
                sendMailAskForConfirmation(user); 
                
                flash("success", Messages.get("register.success")); 
                return redirect(routes.Application.login());
            } catch (EmailException e) {
                Logger.debug("Signup.save Cannot send email", e); 
                flash("error", Messages.get("error.sending.email")); 
            } catch (Exception e) { 
                Logger.error("Signup.save error", e); 
                flash("error", Messages.get("error.technical"));
            } 
            return badRequest(views.html.register.render(registerForm));
        }
    } 

    /**
     * Check if the email already exists.
     *
     * @param registerForm User Form submitted
     * @param email        email address
     * @return Index if there was a problem, null otherwise
     */
    private Result checkBeforeSave(Form<Register> registerForm, String email) {
        // Check unique email
        if (User.findByEmail(email) != null) {
            flash("error", Messages.get("error.email.already.exist"));
            return badRequest(views.html.register.render(registerForm));
        }

        return null;
    }
    
    /**
    * Send the welcome Email with the link to confirm. 
    * 
    * @param user user created 
    * @throws EmailException Exception when sending mail */ 
    private void sendMailAskForConfirmation(User user) throws EmailException, MalformedURLException {
        String subject = Messages.get("mail.confirm.subject"); 
        String urlString = "http://" + Configuration.root().getString("server.hostname"); 
        urlString += "/confirm/" + user.confirmationToken; URL url = new URL(urlString); // validate the URL, will throw an exception if bad. 
        
        String message = Messages.get("mail.confirm.message", url.toString()); 
        
        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email);
        Mail mailer = new Mail(mailerClient); mailer.sendMail(envelop); 
    } 
    
    /**
    * Validate an account with the url in the confirm mail. 
    *
    * @param token a token attached to the user we're confirming. 
    * @return Confirmationpage */ 
    
    @Transactional public Result confirm(String confirmToken) { 
        User user = User.findByConfirmationToken(confirmToken); 
        if (user == null) { 
            flash("error", Messages.get("error.unknown.email")); 
            return badRequest(views.html.confirm.render()); 
        } if (user.validated) { 
            flash("error", Messages.get("error.account.already.validated")); 
            return badRequest(views.html.confirm.render()); 
        } try { 
            if (User.confirm(user)) { 
                sendMailConfirmation(user); 
                flash("success", Messages.get("account.successfully.validated")); 
                return ok(views.html.confirm.render()); 
            } else { 
                Logger.debug("Login.confirm cannot confirm user"); 
                flash("error", Messages.get("error.confirm"));
                return badRequest(views.html.confirm.render());
            }
        } catch (EmailException e) { 
            Logger.debug("Cannot send email", e);
            flash("error", Messages.get("error.sending.confirm.email"));
        } catch (Exception e) { 
            Logger.error("Cannot signup", e); 
            flash("error", Messages.get("error.technical")); 
        } return badRequest(views.html.confirm.render()); 
    } 
    
    /** 
    * Send the confirm mail. 
    * 
    * @param user user created
    * @throws EmailException Exception when sending mail */
    private void sendMailConfirmation(User user) throws EmailException { 
        String subject = Messages.get("mail.welcome.subject"); 
        String message = Messages.get("mail.welcome.message"); 
        Mail.Envelop envelop = new Mail.Envelop(subject, message, user.email); 
        Mail mailer = new Mail(mailerClient); mailer.sendMail(envelop);
    }
}
        

