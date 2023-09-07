package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.dtos.AccountDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AccountController {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Random random = new Random();

    @RequestMapping("/accounts")
    public List<AccountDTO> getAccounts() {
        return accountRepository.findAll().stream().map(AccountDTO::new).collect(Collectors.toList());
    }

    @RequestMapping("/accounts/{id}")
    public AccountDTO getAccount(@PathVariable Long id) {
        Optional<Account> account = accountRepository.findById(id);
        return account.map(AccountDTO::new).orElse(null);
    }

    @RequestMapping(path = "/clients/current/accounts", method = RequestMethod.POST)
    public ResponseEntity<Object> createAccount(Authentication authentication) {

        Client client =  clientRepository.findByEmail(authentication.getName());

        if (client.getAccounts().size() < 3){
            String number;
            Optional<Account> accountRecovered;

            do{
                number = "VIN" + random.nextInt(999);
                accountRecovered = Optional.ofNullable(accountRepository.findByNumber(number));
            }while(accountRecovered.isPresent());


            double balance = 0.0;
            Account account = new Account(number, LocalDate.now(), balance);
            client.addAccount(account);
            accountRepository.save(account);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @RequestMapping(path = "/clients/current/accounts")
    public List<AccountDTO> getAccounts(Authentication authentication) {
        Client client = clientRepository.findByEmail(authentication.getName());

        return client.getAccounts().stream().map(AccountDTO::new).collect(Collectors.toList());
    }
}
