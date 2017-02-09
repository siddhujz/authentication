package controllers;

import models.User;
import play.data.FormFactory;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import javax.persistence.*;
import java.util.List;

import static play.libs.Json.toJson;

public class Dashboard extends Controller {

    private final FormFactory formFactory;
    private final JPAApi jpaApi;

    @Inject
    public Dashboard(FormFactory formFactory, JPAApi jpaApi) {
        this.formFactory = formFactory;
        this.jpaApi = jpaApi;
    }

    @Security.Authenticated(Secured.class)
    @Transactional(readOnly = true)
    public Result index() {
        String email = ctx().session().get("email");
        if (email != null) {
            Query q = jpaApi.em().createQuery("SELECT u FROM User u WHERE u.email = :email");
            q.setParameter("email", email);
            try{
                User user = (User) q.getSingleResult();
                if (user != null) {
                    flash("success", Messages.get("you.have.been.logged.in"));
                    return ok(views.html.user.index.render(Messages.get("user.home.title"), user));
                }
            } catch(Exception e){
                System.out.println("Exception e = " + e.getMessage());
                return redirect(routes.Application.login());
            }
            return redirect(routes.Application.login());
        } else {
            return redirect(routes.Application.login());
        }
    }

}