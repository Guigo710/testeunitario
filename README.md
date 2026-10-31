# Projeto de Testes Unitários com Java e Spring Boot

Este projeto demonstra como testar unidades específicas de uma aplicação Java utilizando **JUnit 5** e **Mockito**. Os exemplos trabalham com dois serviços: uma calculadora de desconto e um serviço responsável por solicitar o envio de uma notificação por e-mail.

A proposta é compreender como verificar regras de negócio de forma isolada, sem depender de serviços externos ou executar toda a aplicação para cada teste.

---

## 1. Objetivos do projeto

Ao estudar este projeto, você vai entender como:

- testar o resultado de um método;
- verificar se uma regra de negócio rejeita dados inválidos;
- simular uma dependência externa com Mockito;
- verificar se um método de uma dependência foi chamado com os parâmetros esperados;
- organizar os testes separadamente do código principal;
- executar os testes com Maven.

## 2. O que é um teste unitário?

Um **teste unitário** verifica uma pequena unidade de código — normalmente um método ou uma classe — de maneira isolada. O objetivo é conferir se ela se comporta conforme as regras esperadas para determinadas entradas.

Imagine uma calculadora que recebe o preço de um produto e um percentual de desconto. Em vez de executar toda a aplicação, abrir uma tela ou acessar um banco de dados, podemos instanciar a calculadora diretamente, chamar o método e comparar o resultado com o valor esperado.

Por exemplo:

```java
CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();
double resultado = calculadora.aplicarDesconto(200.0, 25.0);

assertEquals(150.0, resultado, 0.001);
```

Nesse exemplo, o teste verifica se um produto de R$ 200,00 com 25% de desconto resulta em R$ 150,00.

### Por que testar unidades isoladas?

Testes unitários ajudam a encontrar defeitos mais cedo, documentam o comportamento esperado do código e dão mais segurança para alterar ou refatorar uma classe. Como normalmente não precisam de banco de dados, rede ou outros sistemas, tendem a ser rápidos e previsíveis.

Um teste unitário não garante que o sistema inteiro funciona corretamente. Ele verifica apenas os comportamentos cobertos pelos cenários escritos. Por isso, projetos maiores também precisam de testes de integração e outros tipos de validação.

## 3. Tecnologias utilizadas

| Tecnologia | Função neste projeto |
|---|---|
| Java 17 | Linguagem e versão configurada no projeto. |
| Spring Boot 4.1.1 | Estrutura da aplicação e gerenciamento dos componentes Spring. |
| Maven | Gerenciamento de dependências e execução do ciclo de build e testes. |
| JUnit 5 | Framework utilizado para organizar e executar os testes. |
| Mockito | Biblioteca usada para criar objetos simulados (*mocks*) e verificar interações. |

As dependências de teste são declaradas no `pom.xml`, por meio de `spring-boot-starter-webmvc-test`, com escopo `test`. O Maven disponibiliza essas bibliotecas durante a execução dos testes, sem torná-las dependências de execução da aplicação.

## 4. Estrutura do projeto

```text
src
├── main
│   ├── java/com/example/testeunitario
│   │   ├── EmailService.java
│   │   ├── TesteunitarioApplication.java
│   │   └── service
│   │       ├── CalculadoraDeDesconto.java
│   │       └── NotificacaoService.java
│   └── resources
│       └── application.properties
└── test
    └── java/com/example/testeunitario
        ├── TesteunitarioApplicationTests.java
        └── service
            ├── CalculadoraDeDescontoTest.java
            └── NotificacaoServiceTest.java
```

A pasta `src/main` contém o código da aplicação. A pasta `src/test` contém os testes. Essa separação permite testar o código sem misturar as classes de teste com as classes utilizadas pela aplicação.

## 5. Código que será testado

### 5.1 CalculadoraDeDesconto

Arquivo: `src/main/java/com/example/testeunitario/service/CalculadoraDeDesconto.java`

A classe recebe o valor original e o percentual de desconto. Se algum dos dois valores for negativo, lança uma `IllegalArgumentException`. Caso contrário, calcula o preço com desconto:

```java
@Service
public class CalculadoraDeDesconto {
    public double aplicarDesconto(double valor, double percentual) {
        if (valor < 0 || percentual < 0) {
            throw new IllegalArgumentException("Valores não podem ser negativos");
        }

        return valor - (valor * percentual / 100.0);
    }
}
```

A fórmula utilizada é:

\[
\text{preço final} = \text{valor} - \left(\text{valor} \times \frac{\text{percentual}}{100}\right)
\]

A anotação `@Service` identifica a classe como um componente de serviço do Spring. Nos testes unitários apresentados, porém, a classe é criada diretamente com `new`, sem precisar iniciar o contexto do Spring.

### 5.2 NotificacaoService

Arquivo: `src/main/java/com/example/testeunitario/service/NotificacaoService.java`

Essa classe recebe uma dependência do tipo `EmailService` pelo construtor. Quando o método `notificarUsuario` recebe um e-mail válido, solicita o envio da mensagem. Quando recebe `null` ou uma string vazia/em branco, lança uma exceção.

```java
@Service
public class NotificacaoService {
    private final EmailService emailService;

    public NotificacaoService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notificarUsuario(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail inválido");
        }

        emailService.enviar(
            email,
            "Bem-vindo!",
            "Sua conta foi criada com sucesso"
        );
    }
}
```

A classe não envia o e-mail diretamente. Ela delega essa responsabilidade à interface `EmailService`:

```java
public interface EmailService {
    void enviar(String destinatario, String assunto, String corpo);
}
```

Essa separação facilita substituir a implementação real do envio por um objeto simulado durante os testes.

## 6. Padrão Arrange, Act e Assert (AAA)

O teste da calculadora utiliza uma organização bastante comum chamada **Arrange, Act e Assert**:

1. **Arrange (Preparar):** cria os objetos e prepara os dados necessários.
2. **Act (Executar):** chama o método que será testado.
3. **Assert (Verificar):** compara o comportamento obtido com o comportamento esperado.

Essa divisão deixa claro o que está sendo preparado, qual ação está sendo avaliada e qual resultado o teste espera.

## 7. Testes da calculadora de desconto

Arquivo: `src/test/java/com/example/testeunitario/service/CalculadoraDeDescontoTest.java`

### 7.1 Cenário: calcular o desconto corretamente

```java
@Test
void testAplicarDesconto() {
    // Arrange
    CalculadoraDeDesconto calc = new CalculadoraDeDesconto();

    // Act
    double resultado = calc.aplicarDesconto(200.0, 25.0);

    // Assert
    assertEquals(150.0, resultado, 0.001);
}
```

O que acontece:

- o teste cria uma instância da calculadora;
- chama `aplicarDesconto` com `200.0` e `25.0`;
- verifica se o resultado é `150.0`.

O método `assertEquals` compara o valor esperado com o valor obtido. O terceiro argumento (`0.001`) é a tolerância aceita na comparação de números `double`, que podem apresentar pequenas diferenças de precisão.

### 7.2 Cenário: rejeitar valores negativos

```java
@Test
void deveLancarExcecaoQuandoValoresNegativos() {
    CalculadoraDeDesconto calc = new CalculadoraDeDesconto();

    assertThrows(IllegalArgumentException.class, () -> {
        calc.aplicarDesconto(-100.0, 10.0);
    });
}
```

Nesse teste, o valor do produto é negativo. O método `assertThrows` verifica se a execução lança uma exceção do tipo `IllegalArgumentException`.

O trecho `() -> { ... }` é uma expressão lambda que representa a ação que o JUnit deve executar para verificar a exceção.

**Importante:** a implementação também rejeita percentual negativo, mas o teste atual cobre especificamente o caso de valor do produto negativo. Um teste adicional poderia validar o percentual negativo separadamente.

## 8. Testes do serviço de notificação e uso do Mockito

Arquivo: `src/test/java/com/example/testeunitario/service/NotificacaoServiceTest.java`

O `NotificacaoService` depende de `EmailService`. Em produção, essa interface precisaria ter uma implementação responsável por realizar o envio. Para testar o serviço sem enviar e-mails reais, usamos um **mock**.

### 8.1 O que é um mock?

Um mock é um objeto simulado que substitui uma dependência real durante o teste. Com Mockito, podemos controlar ou observar como o código interage com esse objeto.

Neste projeto, o mock substitui o serviço de e-mail. Assim, o teste consegue verificar se o `NotificacaoService` pediu o envio correto sem conectar a um servidor de e-mail ou enviar uma mensagem de verdade.

### 8.2 Cenário: verificar se o serviço de e-mail foi chamado

```java
@Test
void deveChamarEmailServiceAoNotificar() {
    // Arrange
    EmailService mockEmail = Mockito.mock(EmailService.class);
    NotificacaoService notificacao = new NotificacaoService(mockEmail);

    // Act
    notificacao.notificarUsuario("ana@exemplo.com");

    // Assert
    Mockito.verify(mockEmail).enviar(
        Mockito.eq("ana@exemplo.com"),
        Mockito.eq("Bem-vindo!"),
        Mockito.anyString()
    );
}
```

Entendendo as instruções:

- `Mockito.mock(EmailService.class)`: cria o objeto simulado da interface `EmailService`.
- `new NotificacaoService(mockEmail)`: injeta o mock no serviço que será testado.
- `notificarUsuario(...)`: executa o comportamento que estamos avaliando.
- `Mockito.verify(mockEmail)`: verifica se o mock recebeu a chamada esperada.
- `Mockito.eq(...)`: exige que o argumento seja igual ao valor informado.
- `Mockito.anyString()`: aceita qualquer argumento do tipo `String` para o corpo do e-mail.

O teste comprova que o serviço solicitou o envio para `ana@exemplo.com`, com o assunto `Bem-vindo!` e um corpo em formato `String`. Ele **não** comprova que um e-mail foi efetivamente enviado ou entregue.

### 8.3 Cenário: rejeitar um e-mail inválido

```java
@Test
void deveLancarErroParaEmailInvalido() {
    EmailService mockEmail = Mockito.mock(EmailService.class);
    NotificacaoService notificacao = new NotificacaoService(mockEmail);

    assertThrows(IllegalArgumentException.class, () -> {
        notificacao.notificarUsuario("");
    });
}
```

O teste passa uma string vazia e verifica se o serviço lança `IllegalArgumentException`. A validação da classe também rejeita `null` e strings compostas apenas por espaços, mas esses casos ainda não aparecem como testes separados no projeto.

## 9. Teste de carregamento do contexto Spring

Arquivo: `src/test/java/com/example/testeunitario/TesteunitarioApplicationTests.java`

```java
@SpringBootTest
class TesteunitarioApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

Esse teste é diferente dos testes unitários anteriores. A anotação `@SpringBootTest` solicita que o Spring Boot inicialize o contexto da aplicação. O método vazio verifica, na prática, se a inicialização consegue terminar sem falhas.

Ele é mais próximo de um teste de contexto/integração do que de um teste unitário isolado, pois envolve a configuração e a criação dos componentes gerenciados pelo Spring.

**Atenção ao estado atual do projeto:** `NotificacaoService` exige um objeto `EmailService` no construtor, mas o código fornecido contém apenas a interface, sem uma implementação registrada como componente Spring ou um bean de configuração. Por isso, o teste `contextLoads` pode falhar ao iniciar o contexto por não encontrar um bean de `EmailService`. Os testes unitários de `NotificacaoService` não têm esse problema porque fornecem manualmente um mock ao construtor.

Para fazer o contexto subir, será necessário disponibilizar uma implementação de `EmailService` registrada no Spring ou configurar um bean simulado para esse teste. Isso é uma configuração adicional, não algo que os testes unitários atuais façam automaticamente.

## 10. Como executar os testes

### Pré-requisitos

- JDK 17 instalado e configurado;
- acesso ao terminal;
- Maven Wrapper do projeto (`mvnw` e `mvnw.cmd`), já incluído no repositório.

Abra o terminal na pasta raiz do projeto, onde está o arquivo `pom.xml`.

**Windows (PowerShell ou Prompt de Comando):**

```bat
mvnw.cmd test
```

**Linux ou macOS:**

```bash
./mvnw test
```

Para limpar os arquivos gerados anteriormente e executar os testes novamente:

**Windows:**

```bat
mvnw.cmd clean test
```

**Linux ou macOS:**

```bash
./mvnw clean test
```

O Maven compila o código principal, compila o código de teste e executa os testes encontrados em `src/test`. O resultado é apresentado no terminal. Os relatórios detalhados ficam em `target/surefire-reports`.

Também é possível executar cada classe de teste diretamente pela interface da IDE, como VS Code ou IntelliJ IDEA, desde que as extensões e ferramentas Java estejam configuradas.

## 11. Como interpretar o resultado

- **BUILD SUCCESS:** o Maven concluiu a execução sem falhas de build ou testes.
- **Tests run:** quantidade de testes executados.
- **Failures:** testes que foram executados, mas tiveram resultados diferentes dos esperados.
- **Errors:** testes que não conseguiram concluir normalmente, por exemplo, devido a uma exceção inesperada ou falha ao inicializar o contexto.
- **Skipped:** testes que foram ignorados.

Um build bem-sucedido significa que os testes executados passaram; não significa que todos os comportamentos possíveis do sistema foram testados.
