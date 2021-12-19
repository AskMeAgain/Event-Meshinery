# DataContext

A dataContext is the event data which gets passed to processors and is used for signaling the next step in your
application.

It implements the DataContext interface, which just taks a String id field to give each DataContext a complete unique
id.

Each id should be completely unique and represent a single business object. Like a request for a user creation (which is
done via multiple restcalls/db inserts etc).

## Changelog

In the beginning a dataContext is completely empty and is getting filled with data while stepping through the
application. If your processing pipeline is getting bigger over time, you have complete access to all data written
inside.

## Example

A user creates a new account. Expected throughput is not so high. We use Mysql as state store.

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    @AllArgsConstructor
    public class UserCreationDataContext implements DataContext {
        String id;
        InputData inputData; //like username, email
        String userId; //this will be added later
        String emailId; //this will be added later
    }

### Step 1

Our rest api receives the userCreationRequest and transforms it to a UserCreationDataContext. It writes the
event/dataContext into our mysql table.

We have an event defined which reads from this table and inserts a new user into the db. As a result we get a userId to
unique identify the user:

We write the following dataContext to the state store to signal the next processing step:

    {
        "id": "1",
        "inputData": {
            "userName": "WhatANiceUserName",
            "email": "NiceUserName@gmx.de"
        },
        "userId": "693049523"
    }

### Step 2

The next event is listening on this specific event/dataContext and triggers an email to that user to confirm the email.

When sending the email we get an emailId, so we can trace the email in our system.

We send the following dataContext to the next process:

    {
        "id": "1",
        "inputData": {
            "userName": "WhatANiceUserName",
            "email": "NiceUserName@gmx.de"
        },
        "userId": "693049523",
        "emailId": "123051592"
    }

### Step 3

This can go on and on. The good thing is you never lose your data and you have a complete computation log about your
application. You see exactly what thing got added in which step. And when you need to add something at the end, you have
complete access to all the data which got computed before.