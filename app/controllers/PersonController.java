package controllers;

import models.Person;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;

import static play.libs.Json.toJson;

public class PersonController extends Controller {

    private final FormFactory formFactory;
    private final JPAApi jpaApi;

    @Inject
    public PersonController(FormFactory formFactory, JPAApi jpaApi) {
        this.formFactory = formFactory;
        this.jpaApi = jpaApi;
    }

    public Result person() {
        return ok(views.html.person.render("Your new application is ready."));
    }

    @Transactional
    public Result addPerson() {
        Person person = formFactory.form(Person.class).bindFromRequest().get();
        jpaApi.em().persist(person);
        return redirect(routes.PersonController.person());
    }

    @Transactional(readOnly = true)
    public Result getPersons() {
        List<Person> persons = (List<Person>) jpaApi.em().createQuery("select p from Person p").getResultList();
        return ok(toJson(persons));
    }

}