package io.openliberty.sample.jakarta.annotations;

import random.test.pkg.on.PostConstruct;

@Resource(type = Object.class, name = "aa")
public class IncorrectPostConstructAnnotation {

    @random.test.pkg.on.PostConstruct
    public void getHappinessRandom(String type) {

    }

}