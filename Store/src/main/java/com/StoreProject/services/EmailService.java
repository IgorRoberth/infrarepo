package com.StoreProject.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "email.enabled", havingValue = "true")
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendPasswordResetEmail(String email, String token, String userName, String userType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Contemporary - Redefinir Senha");
            message.setFrom(fromEmail);
            
            // DUAS OPÇÕES: LINK COM TOKEN OU CÓDIGO PARA COPIAR
            String resetLink = frontendUrl + "/resetpassword/reset-password.html?token=" + token;
            
            String emailBody = String.format(
           "CONTEMPORARY - REDEFINIR SENHA\n\n" +
           "Olá %s,\n\n" +
           "Recebemos uma solicitação para redefinir a senha da sua conta de %s.\n\n" +
           "Código de Verificação:\n" +
           "Cole este código: %s\n\n" +
           "Este código expira em 1 hora.\n\n" +
           "Se você não solicitou esta alteração, ignore este email.\n\n" +
           "Atenciosamente,\n" +
           "Equipe Contemporary",
           userName,
           userType.equals("SELLER") ? "Vendedor" : "Cliente",
           token
           );
            
            message.setText(emailBody);
            
            System.out.println("Enviando email com código: " + token.substring(0, 10) + "...");
            mailSender.send(message);
            System.out.println("Email enviado com SUCESSO!");
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar email de reset de senha", e);
        }
    }

    public void testEmailConnection() {
        try {
            System.out.println("Testando conexão com servidor de email...");
            System.out.println("Email configurado: " + fromEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(fromEmail); 
            message.setSubject("Teste de Conexão - Contemporary");
            message.setFrom(fromEmail);
            message.setText("Este é um teste de conexão com o servidor de email.\n\nSe você recebeu este email, a configuração está funcionando!\n\nData/Hora: " + 
                           java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            mailSender.send(message);
            System.out.println("TESTE DE CONEXÃO: SUCESSO!");
            
        } catch (Exception e) {
            System.err.println("TESTE DE CONEXÃO: FALHOU!");
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            throw e; 
        }
    }

    //  ADICIONAR ESTE MÉTODO SE NÃO EXISTIR
    public void sendTestEmail(String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Contemporary - Teste Mailtrap");
            message.setFrom(fromEmail);
            
            String emailBody = String.format(
                "TESTE MAILTRAP - CONTEMPORARY\n\n" +
                "Este é um email de teste enviado via Mailtrap.\n\n" +
                "Se você vê este email no Mailtrap, a configuração está CORRETA!\n\n" +
                "Enviado de: %s\n" +
                "Provedor: Mailtrap\n" +
                "Data/Hora: %s\n\n" +
                "Equipe Contemporary",
                fromEmail,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Email de TESTE Mailtrap enviado para: " + email);
            
        } catch (Exception e) {
            System.err.println("Erro no TESTE Mailtrap: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void testEmailConnectionMailtrap() {
        try {
            System.out.println("Testando conexão Mailtrap...");
            System.out.println("Email configurado: " + fromEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(fromEmail);
            message.setSubject("Teste de Conexão Mailtrap - Contemporary");
            message.setFrom(fromEmail);
            message.setText("Este é um teste de conexão com Mailtrap.\n\nSe você vê este email, a configuração está funcionando!");
            
            mailSender.send(message);
            System.out.println("TESTE Mailtrap: SUCESSO!");
            
        } catch (Exception e) {
            System.err.println("TESTE Mailtrap: FALHOU!");
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // ADICIONAR ESTE MÉTODO NO EmailService
    public void sendPasswordChangeConfirmation(String email, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Contemporary - Senha Alterada");
            message.setFrom(fromEmail);
            
            String emailBody = String.format(
                "CONTEMPORARY - CONFIRMAÇÃO\n\n" +
                "Olá %s,\n\n" +
                "Sua senha foi alterada com sucesso!\n\n" +
                "Data/Hora: %s\n\n" +
                "Se você não fez esta alteração, entre em contato conosco imediatamente.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Contemporary",
                userName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            System.out.println("Email de confirmação enviado para: " + email);
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de confirmação: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar email de confirmação", e);
        }
    }
}