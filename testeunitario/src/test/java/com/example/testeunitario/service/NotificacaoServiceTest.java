package com.example.testeunitario.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.testeunitario.EmailService;

public class NotificacaoServiceTest {
    @Test
    void deveChamarEmailServiceAoNotificar() {
        //Arrange
        EmailService mockEmail = Mockito.mock(EmailService.class);
        NotificacaoService notificacao = new NotificacaoService(mockEmail);

        //Act
        notificacao.notificarUsuario("ana@exemplo.com");

        //Assert
        Mockito.verify(mockEmail).enviar(
                Mockito.eq("ana@exemplo.com"),
                Mockito.eq("Bem-vindo!"),
                Mockito.anyString()
        );
    }
    @Test 
    void deveLancarErroParaEmailInvalido(){
        EmailService mockEmail = Mockito.mock(EmailService.class);
        NotificacaoService notificacao = new NotificacaoService(mockEmail);

        assertThrows(IllegalArgumentException.class,() ->{
            notificacao.notificarUsuario("");
        });
    }
}
