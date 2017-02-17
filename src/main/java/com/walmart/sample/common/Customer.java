package com.walmart.sample.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The Customer class represents a customer requesting a reservation.
 */
@Getter
@Setter
@Builder
public class Customer {


    //TODO Further define customer

    /**
     * The first name of the customer.
     */
    private String firstName;


    /**
     * The last name of the customer.
     */
    private String lastName;
}
