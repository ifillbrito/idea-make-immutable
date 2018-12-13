# Make Immutable IDEA-Plugin
This plugin makes a class immutable by doing the following refactoring:
     
- The class modifier "final" is added is not present.
- All fields modifier "final" is added is not present.
- The constructor visibility is changed to private.
- A static constructor is generated (method name: "of") based on the private constructor.
- Getters are generated.
- With-Methods are generated. This methods allow creating a new immutable object based on the current one.

## Example
Let's assume you have the following class (no getters and setters):
```java
public class Person {
    
    private String lastName;
    private String firstName;
    private LocalDate birthday;

    public Person(String lastName, String firstName, LocalDate birthday) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
    }
}
```
Then, if you run the plugin 'Make Immutable' you would obtain the following result:
```java
public final class Person {

    private final String lastName;
    private final String firstName;
    private final LocalDate birthday;

    private Person(String lastName, String firstName, LocalDate birthday) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
    }

    public static Person of(String lastName, String firstName, LocalDate birthday) {
        return new Person(lastName, firstName, birthday);
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public Person withLastName(String lastName) {
        return of(lastName, getFirstName(), getBirthday());
    }

    public Person withFirstName(String firstName) {
        return of(getLastName(), firstName, getBirthday());
    }

    public Person withBirthday(LocalDate birthday) {
        return of(getLastName(), getFirstName(), birthday);
    }

}
``` 
## TODOs
[ ] Validations: no validations are made. If the class is already immutable,
the static constructor, the getters and the with-methods will be generated anyway.

## License

Copyright 2018 by Grebiel José Ifill Brito

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.