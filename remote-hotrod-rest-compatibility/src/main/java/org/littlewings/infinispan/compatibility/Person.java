package org.littlewings.infinispan.compatibility;

import java.io.Serializable;
import java.util.Objects;

public class Person implements Serializable {
    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Person) {
            Person p = (Person) other;
            return Objects.equals(firstName, p.firstName) && Objects.equals(lastName, p.lastName);
        } else {
            return false;
        }
    }
}
