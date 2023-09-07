package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.dtos.ClientDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ClientController {

    private Random random = new Random();
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/clients")
    public List<ClientDTO> getClients(){
        return clientRepository.findAll().stream().map(ClientDTO::new).collect(Collectors.toList());
    }

    @RequestMapping(path = "/clients", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String firstName,
                                           @RequestParam String lastName,
                                           @RequestParam String email,
                                           @RequestParam String password) {

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (clientRepository.findByEmail(email) != null) {
            return new ResponseEntity<>("E-mail already in use", HttpStatus.FORBIDDEN);
        }

        Client client = new Client(firstName, lastName, email, passwordEncoder.encode(password));
        Account account = new Account("VIN"+random.nextInt(999), LocalDate.now(), 0.0);
        client.addAccount(account);

        clientRepository.save(client);
        accountRepository.save(account);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @RequestMapping("/clients/current")
    public ClientDTO getCurrent(Authentication authentication) {
        Client client =  clientRepository.findByEmail(authentication.getName());

        return new ClientDTO(client);
    }


    @RequestMapping("/clients/{id}")
    public ClientDTO getClient(@PathVariable Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.map(ClientDTO::new).orElse(null);
    }
}
