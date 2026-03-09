package com.project.hyunso.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Person {
    private Long id;
    private String name;
    private int age;
    private List<String> hobbies;
}
