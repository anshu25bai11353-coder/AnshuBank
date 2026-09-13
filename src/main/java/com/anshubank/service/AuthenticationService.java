package com.anshubank.service;

import com.anshubank.repository.*;
import com.anshubank.model.Customer;
import com.anshubank.security.SecurityUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {

    private final CustomerRepository customers = new CustomerRepository();
    private final AdminRepository admins = new AdminRepository();

    public Optional<Customer> customer(
            String id,
            String password
    ) throws SQLException {

        Optional<Customer> c = customers.findById(id);

        return c.filter(
                x -> SecurityUtil.matches(
                        password,
                        x.getPasswordHash()
                )
        );
    }

    public boolean admin(
            String id,
            String password
    ) throws SQLException {

        return admins.authenticate(id, password);
    }
}