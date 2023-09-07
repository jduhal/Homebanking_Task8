package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.dtos.ClientDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.models.Transaction;
import com.mindhub.homebanking.models.TransactionType;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import com.mindhub.homebanking.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    @RequestMapping(path = "/transactions", method = RequestMethod.POST)
    public ResponseEntity<Object> makeTransaction (@RequestParam double amount,
                                                   @RequestParam String description,
                                                   @RequestParam String fromAccountNumber,
                                                   @RequestParam String toAccountNumber,
                                                   Authentication authentication) {

        if (amount <= 0 || description.isEmpty() || fromAccountNumber.isEmpty() || toAccountNumber.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }

        if (fromAccountNumber.equalsIgnoreCase(toAccountNumber)) {
            return new ResponseEntity<>("Origin and destiny are equals", HttpStatus.FORBIDDEN);
        }

        Client clientOrigin = clientRepository.findByEmail(authentication.getName());

        Optional<Account> originAccountOptional = Optional.ofNullable(accountRepository.findByNumber(fromAccountNumber));
        Account originAccount;
        if (originAccountOptional.isPresent()) {
            originAccount = originAccountOptional.get();
        } else {
            return new ResponseEntity<>("Invalid origin account", HttpStatus.FORBIDDEN);
        }

        Optional<Account> destinyAccountOptional = Optional.ofNullable(accountRepository.findByNumber(toAccountNumber));
        Account destinyAccount;
        if (destinyAccountOptional.isPresent()) {
            destinyAccount = destinyAccountOptional.get();
        } else {
            return new ResponseEntity<>("Invalid destiny account", HttpStatus.FORBIDDEN);
        }

        if (originAccount.getBalance() < amount){
            return new ResponseEntity<>("Insufficient founds", HttpStatus.FORBIDDEN);
        }

        Transaction debit = new Transaction(TransactionType.DEBIT, -amount, description, LocalDateTime.now());
        Transaction credit = new Transaction(TransactionType.CREDIT, amount, description, LocalDateTime.now());

        originAccount.addTransaction(debit);
        originAccount.substractAmount(amount);

        destinyAccount.addTransaction(credit);
        destinyAccount.addAmount(amount);

        transactionRepository.save(debit);
        transactionRepository.save(credit);
        accountRepository.save(originAccount);
        accountRepository.save(destinyAccount);

        return new ResponseEntity<>("Transaction resolved", HttpStatus.CREATED);


    }
}
