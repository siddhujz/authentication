package controllers;

import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import javax.persistence.*;
import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {

    private final FormFactory formFactory;
    private final JPAApi jpaApi;

    @Inject
    public Application(FormFactory formFactory, JPAApi jpaApi) {
        this.formFactory = formFactory;
        this.jpaApi = jpaApi;
    }
    
    public static class Login {
        @Constraints.Required
        public String email;
        @Constraints.Required
        public String password;

        public String validate() {
            User user = new User();
            user = User.authenticate(email, password);
            if (user == null) {
                return "Invalid user or password";
            } else if(!user.validated) {
                return "Account is not yet confirmed!";
            }
            return null;
        }
    }

    public Result index() {
        return ok(views.html.index.render("Your new application is ready."));
    }
    
    public Result login() {
        Form<Login> loginForm = formFactory.form(Login.class);
        return ok(views.html.login.render(loginForm));
    }

    @Transactional(readOnly = true)
    public Result authenticate() {
        Form<Login> form = formFactory.form(Login.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(views.html.login.render(form));
        } else {
            session().clear();
            session("email", form.get().email);
            return redirect(routes.Dashboard.index());
        }
    }

    public Result logout() {
        session().clear();
        flash("success", Messages.get("you.have.been.logged.out"));
        return redirect(routes.Application.login());
    }
}


