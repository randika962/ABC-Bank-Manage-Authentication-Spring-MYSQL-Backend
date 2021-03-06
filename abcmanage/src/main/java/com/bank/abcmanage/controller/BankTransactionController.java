package com.bank.abcmanage.controller;

import java.io.IOException;
import java.util.List;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import ch.qos.logback.classic.Logger;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.filters.AddDefaultCharsetFilter.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.abcmanage.model.BankAccount;
import com.bank.abcmanage.model.BankResponse;
import com.bank.abcmanage.model.BankTransaction;
import com.bank.abcmanage.model.BankUser;
import com.bank.abcmanage.services.BankAccountService;
import com.bank.abcmanage.services.BankPdfExporter;
import com.bank.abcmanage.services.BankTransactionService;
import com.lowagie.text.DocumentException;

@RestController
@CrossOrigin(origins = "*")
public class BankTransactionController {
      
	@Autowired
	private BankTransactionService transactionService;
	
//	@Autowired
//	private BankPdfExporter pdfService;
	
	@Autowired
	private BankAccountService accountService;
	
	//endpoint for getting all transactions
    @GetMapping("/banktransaction")
	public ResponseEntity list ()
	{
		return ResponseEntity.status(HttpStatus.OK)
				.body(transactionService.listAllBankTransaction());//listAllBankTransaction());
	} 
	
	
	//endpoint for deposit funds
	@PostMapping("/deposit")
	public String createDeposit(@RequestBody BankTransaction transaction) {
		System.out.println(transaction.toString());
		int aid = transaction.getSourceAccId();//getSourceAccId();
		System.out.println("aid "+aid);
		float tranamount = transaction.getTransacAmount();
		
		BankAccount depositacc = accountService.getBankAccountById(aid);
		float newamount = (accountService.getBankAccountById(aid).getaBalance() + (float) tranamount);
		
		accountService.incrementBalance(depositacc, newamount);
		transactionService.createBankTransaction(transaction);
		return "Success Deposit";
	}
	
	
	// endpoint for withdrawal funds
	@PostMapping("/withdraw")
	public String createWithdraw(@RequestBody BankTransaction transaction) {
		int aid = transaction.getSourceAccId();
		float tranamount = transaction.getTransacAmount();
		
		BankAccount depositacc = accountService.getBankAccountById(aid);
		
		BankAccount accou = accountService.getBankAccountById(aid);
		if (accou.getaBalance()<=0 || accou.getaBalance()<=tranamount) {
			return "Sorry! You haven't enough balance.";
		}else {
			float newamount = (accou.getaBalance() - (float) tranamount);
			accountService.decrementBalance(depositacc, newamount);
			transactionService.createBankTransaction(transaction);
			return "Successfully Withdraw!!!!";
		}		
		
//		float newamount = (accountService.getBankAccountById(aid).getaBalance() - tranamount);
		
//		accountService.decrementBalance(depositacc, newamount);
//		transactionService.createBankTransaction(transaction);
//		return "Success Withdraw";
	}
	
	// endpoint for transfer funds
	@PostMapping("/transfer")
	public String createTransfer(@RequestBody BankTransaction transaction) {
		try {
		    System.out.println("Transaction"+transaction.toString());
			int aid = transaction.getSourceAccId();
			int destination_id = transaction.getDestinationAccId();
			float tranamount = transaction.getTransacAmount();
			BankAccount sourceacc = accountService.getBankAccountById(aid);
			BankAccount depositacc = accountService.getBankAccountById(destination_id);
			
		
			if(sourceacc.getaBalance()<=0 || sourceacc.getaBalance()<=tranamount) {
				return "Sorry! Can't Transfer, You haven't enough Account balance";
			}else {
				float newsccBalance = (accountService.getBankAccountById(aid).getaBalance() - (float) tranamount);
				float newdesBalance = (accountService.getBankAccountById(destination_id).getaBalance() + (float) tranamount);
				
				accountService.incrementBalance(sourceacc, newsccBalance);
				accountService.incrementBalance(depositacc, newdesBalance);
				transactionService.createBankTransaction(transaction);
				return "Success Transfer";
			}	
			
		}catch(NoSuchElementException e) {
			if(e != null) {
				return "This user account is not in the system";
			}
			return null;
			
		}
		
		}
	
	@DeleteMapping("/transactiondelete/{tId}")
	public String deleteById(@PathVariable int tId)
	{
		transactionService.deleteBankTransactionById(tId);
		return "Success Transaction Delete";
	}
	
	@GetMapping("/bankstransaction/ABC_Transaction/{id}")
	public void CreateBankPdf(HttpServletResponse response,@PathVariable Integer id) throws DocumentException, IOException {
		
		BankAccount acc =  accountService.getBankAccountById(id);//new
		
		response.setContentType("application/pdf");
		
		BankPdfExporter pdfService = new BankPdfExporter(accountService,acc);
		pdfService.CreateBankPdf(response, id);
		
	}
	
	@GetMapping("/filterbanktransaction/{transacTime}")
	public List<BankTransaction> getfilterBydate(@PathVariable String transacTime ){
		Timestamp DateTime = Timestamp.valueOf(transacTime);
		return transactionService.getBankTransactionBydate(DateTime);
				
	}
	
	@GetMapping("/filterbanktransactionbyDate")
	public List<BankTransaction> getfilterBydate(@RequestParam String stDate, @RequestParam String edDate){
		Timestamp stdDate = Timestamp.valueOf(stDate);
		Timestamp endDate = Timestamp.valueOf(edDate);
		return transactionService.getfiltertransactionBydate(stdDate, endDate);
	}


	
	
	
}
