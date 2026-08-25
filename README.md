# 🏡 RepHub

Trabalho final de POOWeb II, utilizando SpringBoot e API REST. Gerenciador para repúnlicas
> Ana júlia Bock Medina, SPI-UFSM

O **RepHub** é um sistema para organização e gerenciamento de repúblicas e moradias compartilhadas.

O projeto tem como objetivo reunir em um único ambiente funcionalidades relacionadas aos moradores, tarefas domésticas, despesas, pagamentos e comunicação básica da casa.

Esta será a primeira versão acadêmica do projeto. Ela foi planejada para possuir um conjunto completo de funcionalidades próprias, mas também permitir que o sistema continue evoluindo em novas versões após o desenvolvimento inicial.

## ⚙️ Funcionalidades desta versão

### 👥 Usuários e repúblicas

O sistema permitirá o cadastro de usuários e a criação de repúblicas.

O usuário responsável pela criação da república será automaticamente definido como **ADMIN**.

Outros usuários poderão entrar na república utilizando um código de convite de uso único, recebendo inicialmente o papel de **MORADOR**.

Nesta versão, cada usuário poderá possuir apenas uma participação ativa em uma república por vez.

### ✉️ Convites

Administradores poderão gerar códigos para convidar novos moradores.

Cada código será único e poderá ser utilizado apenas uma vez.

Após sua utilização, uma nova participação será criada para o usuário dentro da república.

### 🧹 Tarefas domésticas

Os moradores poderão cadastrar e acompanhar tarefas domésticas da república.

Uma tarefa poderá possuir:

* título;
* descrição;
* prazo;
* responsável;
* status.

Inicialmente, os estados disponíveis serão **PENDENTE** e **CONCLUÍDA**.

O objetivo é permitir uma organização simples das responsabilidades da casa.

### ⚠️ Quadro de avisos

Cada república possuirá um quadro de avisos simples, inspirado nas tradicionais notas deixadas na geladeira.

Os moradores poderão publicar recados contendo título, mensagem, autor e data de publicação.

### 💸 Despesas

O sistema permitirá cadastrar despesas relacionadas à república, como aluguel, água, energia, internet, mercado, limpeza, manutenção e lazer.

Cada despesa poderá ser dividida entre os moradores de duas formas:

* **igual**, distribuindo o valor entre os participantes selecionados;
* **personalizada**, permitindo definir valores diferentes para cada participante.

O sistema armazenará quanto cada morador deve dentro de uma determinada despesa.

### 💳 Pagamentos

Os moradores poderão registrar pagamentos referentes às suas divisões de despesas.

Serão permitidos pagamentos parciais, permitindo acompanhar:

* valor devido;
* valor já pago;
* valor ainda pendente;
* situação da divisão.

Pagamentos poderão ser registrados por PIX, dinheiro, transferência, cartão ou outras formas.

### 📈 Extrato financeiro

O RepúblicaHub permitirá que cada morador consulte quanto custou morar na república em determinado período.

O extrato poderá ser consultado por:

* mês;
* intervalo de meses;
* ano;
* todo o período.

O sistema poderá apresentar:

* custo do morador no período;
* total já pago;
* valor pendente;
* média mensal;
* maior e menor custo mensal;
* total gasto pela república;
* distribuição das despesas por categoria;
* evolução dos gastos ao longo do tempo;
* aumento ou redução dos custos entre períodos.

O custo individual será calculado de acordo com as despesas realmente atribuídas ao morador.

Assim, uma divisão personalizada não será tratada como uma simples divisão do valor total pelo número de moradores.

## 💻 Tecnologias previstas

O projeto será desenvolvido principalmente utilizando:

* Java;
* Spring Boot;
* Spring Web;
* Spring Data JPA;
* PostgreSQL;
* API REST.

Outras tecnologias e bibliotecas poderão ser adicionadas conforme a evolução do projeto.

## 🗺️ Diagrama de classes
Encontrada em: [Docs/diagrama_de_classes.png](Docs/diagrama_de_classes.png)

## 🌱 Evolução do RepHub

Esta versão representa o primeiro núcleo funcional do sistema.

O objetivo não é considerar o RepHub encerrado após a entrega do trabalho. O projeto continuará evoluindo através de novas versões independentes, cada uma acrescentando novos recursos ao sistema existente.

Entre as possíveis evoluções estão:

* autenticação e autorização mais completas;
* múltiplas repúblicas por usuário;
* gerenciamento avançado de moradores;
* votações;
* histórico e auditoria;
* tarefas com prioridades;
* pontuação por tarefas;
* ranking entre moradores;
* conquistas e gamificação;
* Índice de Caos Republicano;
* gráficos e dashboards mais avançados;
* notificações;
* calendário compartilhado;
* frontend completo;
* testes automatizados;
* Docker e deploy.

Dessa forma, cada nova versão poderá ampliar o RepúblicaHub sem invalidar as funcionalidades já desenvolvidas.

## 🚧 Status

🏗️ **Versão acadêmica inicial em desenvolvimento.**
ver mais em: [Docs](docs/)

O RepúblicaHub foi planejado como um projeto de evolução contínua, permitindo que novas funcionalidades sejam incorporadas gradualmente conforme novos conceitos e tecnologias forem estudados.
