package io.openliberty.sample.jakarta.annotations;

import random.test.pkg.on.Resource;

@Resource(type = Object.class, name = "aa")
public class IncorrectResourceAnnotation {

    private Integer studentId;


	@Resource(shareable = true)
    private boolean isHappy;

	@Resource(name = "test")
    private boolean isSad;


    private String emailAddress;


}

@random.test.pkg.on.Resource(name = "aa")
class PostDoctoralStudent {

    private Integer studentId;


	@random.test.pkg.on.Resource(shareable = true)
    private boolean isHappy;

	@random.test.pkg.on.Resource
    private boolean isSad;


    private String emailAddress;

}