# RepHub
Trabalho final de POOWeb II, utilizando SpringBoot e API REST. Gerenciador para repúnlicas
> Ana júlia Bock Medina, SPI-UFSM

Projeto de sistema web para gerenciamento de repúblicas e moradias compartilhadas, criado com foco em organização, convivência e controle financeiro entre moradores.

A proposta do projeto é centralizar em um único sistema tarefas domésticas, despesas, pagamentos, votações, avisos, histórico de eventos e informações financeiras da república.

Além das funcionalidades tradicionais de gerenciamento, o sistema também contará com recursos de gamificação, como ranking de tarefas concluídas e um **Índice de Caos Republicano**, responsável por representar de forma divertida o nível atual de desorganização da casa.

## Objetivo do projeto

O objetivo do RepúblicaHub é facilitar a administração de uma moradia compartilhada, permitindo que os próprios moradores acompanhem as responsabilidades da casa e tenham maior transparência sobre despesas e atividades.

O projeto também foi pensado como uma aplicação para estudo e prática de desenvolvimento backend utilizando **Spring Boot e API REST**, explorando autenticação, regras de negócio, persistência de dados, documentação de API, testes e outras ferramentas do ecossistema Spring.

## Principais funcionalidades

### Usuários e repúblicas

O sistema permitirá que novos usuários criem uma conta e escolham entre:

* criar uma nova república;
* entrar em uma república existente utilizando um código de convite.

O usuário que criar uma república será automaticamente registrado como **ADMIN**.

Usuários que entrarem através de convite serão registrados como **MORADOR**.

Um mesmo usuário poderá participar de diferentes repúblicas e possuir papéis diferentes em cada uma delas.

### Administração da república

Administradores poderão realizar ações como:

* gerenciar moradores;
* gerar convites;
* remover moradores;
* promover moradores;
* alterar configurações da república.

Os convites possuirão códigos únicos e serão de **uso único**.

### Gestão de despesas

Os moradores poderão cadastrar despesas relacionadas à república, como:

* aluguel;
* água;
* energia;
* internet;
* mercado;
* produtos de limpeza;
* manutenção;
* lazer;
* outras despesas.

As despesas poderão ser divididas de duas formas:

**Divisão igual:** o valor é distribuído igualmente entre os moradores selecionados.

**Divisão personalizada:** cada participante possui um valor específico a pagar.

O sistema deverá controlar o valor devido, o valor já pago e o status de cada divisão.

### Pagamentos

Os pagamentos estarão relacionados às divisões das despesas.

Será possível registrar pagamentos por:

* PIX;
* dinheiro;
* transferência;
* cartão;
* outras formas.

Pagamentos parciais poderão ser registrados.

O sistema também deverá impedir que o valor confirmado ultrapasse o valor devido pelo participante.

### Extrato financeiro

Cada morador poderá acessar seu próprio extrato financeiro e descobrir quanto custou morar na república em determinado período.

As consultas poderão ser feitas por:

* mês;
* intervalo de meses;
* ano;
* todo o período em que o usuário participou da república.

O extrato poderá apresentar informações como:

* custo total no período;
* valor já pago;
* valor pendente;
* média mensal;
* maior custo mensal;
* menor custo mensal;
* evolução dos gastos;
* variação percentual entre períodos;
* distribuição dos gastos por categoria.

O sistema também deverá permitir visualizar a evolução dos custos através de gráficos e indicadores de aumento ou diminuição.

Exemplo:

```text
Julho
R$ 980,00

Agosto
R$ 1.100,00

Variação
+12,24%
```

O custo individual de um morador será calculado com base nas divisões de despesas atribuídas a ele, e não simplesmente pela divisão do total da república pelo número de moradores.

### Tarefas domésticas

Os moradores poderão criar e acompanhar tarefas como:

* lavar a louça;
* limpar a cozinha;
* levar o lixo;
* limpar o banheiro;
* comprar produtos;
* organizar áreas comuns.

Cada tarefa poderá possuir:

* responsável;
* prioridade;
* prazo;
* status;
* pontuação.

Uma tarefa poderá inicialmente não possuir responsável e ser atribuída posteriormente.

### Ranking de tarefas

As tarefas concluídas poderão gerar pontos para os moradores.

Esses pontos serão utilizados para criar um ranking interno da república.

Exemplo:

```text
🏆 Ranking da República

1. Ana      320 pontos
2. Bianca   275 pontos
3. Carla    210 pontos
```

O ranking poderá considerar informações como:

* quantidade de tarefas concluídas;
* pontuação total;
* posição atual;
* participação nas atividades da casa.

### Índice de Caos Republicano

O sistema possuirá um indicador chamado **Índice de Caos Republicano**.

O índice será calculado com base em situações como:

* tarefas atrasadas;
* tarefas pendentes;
* despesas vencidas;
* pagamentos pendentes.

O valor poderá variar entre `0` e `100`.

Exemplo:

```text
0 - 20
República zen

21 - 40
Leve bagunça

41 - 60
Caos administrável

61 - 80
Situação duvidosa

81 - 100
A casa venceu
```

O objetivo dessa funcionalidade é apresentar informações importantes da casa de maneira mais divertida e gamificada.

### Votações

Moradores poderão criar votações para decisões internas.

Exemplos:

```text
Devemos trocar o plano de internet?

Qual será o dia da faxina?

Devemos comprar uma air fryer?

Qual produto devemos comprar para a casa?
```

As votações poderão ser dos tipos:

* SIM/NÃO;
* múltipla escolha.

Cada participante poderá votar apenas uma vez por votação.

Votações encerradas ou canceladas não poderão receber novos votos.

### Quadro de avisos

Cada república possuirá um quadro de avisos simples, funcionando como uma versão digital das tradicionais notas deixadas na geladeira.

Os avisos possuirão:

* título;
* mensagem;
* autor;
* data de publicação.

Exemplo:

```text
Comprar papel higiênico

Acabou de novo.
Quem passar no mercado hoje, por favor compre.
```

### Histórico e auditoria

O sistema manterá um histórico de acontecimentos importantes da república.

Alguns exemplos:

```text
Morador entrou na república

Morador saiu da república

Despesa criada

Despesa alterada

Pagamento realizado

Tarefa concluída

Votação criada

Aviso publicado
```

O histórico possui caráter apenas informativo.

Ele **não será utilizado para restauração de versões ou rollback**.

Registros históricos não poderão ser editados pelos usuários e continuarão disponíveis mesmo depois que um morador deixar a república.

## Papéis do sistema

Inicialmente existirão dois papéis:

### ADMIN

Responsável por administrar a república.

Entre suas permissões estarão:

```text
Gerenciar moradores
Gerenciar convites
Alterar configurações
Remover moradores
Promover moradores
```

### MORADOR

Participante comum da república.

Entre suas ações estarão:

```text
Cadastrar despesas
Realizar pagamentos
Concluir tarefas
Participar de votações
Publicar avisos
Consultar seu próprio extrato
```

## Principais entidades

O domínio inicial será composto pelas seguintes entidades:

```text
Usuario
Republica
ParticipacaoRepublica
ConviteRepublica
Despesa
DivisaoDespesa
Pagamento
TarefaDomestica
Votacao
OpcaoVotacao
Voto
Aviso
HistoricoEvento
```

A entidade `ParticipacaoRepublica` representa o vínculo entre um usuário e uma república.

Isso permite, por exemplo, que um mesmo usuário seja:

```text
ADMIN na República A

MORADOR na República B
```

sem precisar criar subclasses diferentes de usuário.

## Estrutura conceitual
**ver diagrama completo em: [Diagrama de classe](diagrama_classe.png)

```text
                         Usuario
                            |
                            v
                ParticipacaoRepublica
                            |
                            v
                        Republica
                            |
          +-----------------+----------------+
          |                 |                |
          v                 v                v
       Despesa       TarefaDomestica      Votacao
          |                                  |
          v                                  v
   DivisaoDespesa                      OpcaoVotacao
          |                                  |
          v                                  v
      Pagamento                             Voto

                        Republica
                            |
                +-----------+-----------+
                |                       |
                v                       v
              Aviso              HistoricoEvento

                        Republica
                            |
                            v
                    ConviteRepublica
```

## Funcionalidades calculadas

Algumas funcionalidades não representarão diretamente tabelas no banco.

Serão calculadas através de serviços da aplicação.

```text
RankingService

IndiceCaosService

ExtratoFinanceiroService

HistoricoService
```

Esses serviços utilizarão as informações das entidades para produzir resultados para a API.

## Tecnologias previstas

O projeto será desenvolvido utilizando:

```text
Java
Spring Boot
Spring Web
Spring Data JPA
PostgreSQL
Flyway
Swagger / OpenAPI
```

Outras bibliotecas poderão ser adicionadas conforme a evolução do projeto.

## Fluxo inicial do usuário

```text
Criar conta
    |
    v
Escolher uma opção
    |
    +---------------------------+
    |                           |
    v                           v
Criar República          Entrar por convite
    |                           |
    v                           v
Recebe ADMIN              Recebe MORADOR
    |                           |
    +-------------+-------------+
                  |
                  v
              Dashboard
```

## Página inicial

A aplicação deverá possuir uma página pública apresentando o propósito do RepúblicaHub.

A página terá acesso para:

```text
Entrar

Criar conta

Criar uma república

Entrar em uma república por convite
```

## Status

> 🚧 Projeto em desenvolvimento.

## Ideias futuras

Algumas funcionalidades poderão ser adicionadas posteriormente:

```text
Conquistas e medalhas
Sequência de tarefas concluídas
Desafios entre moradores
Calendário compartilhado
Notificações
Metas financeiras
Comparação de gastos entre meses
Relatórios
Histórico do Índice de Caos
Ranking mensal
Estatísticas da república
```

A ideia é que o RepúblicaHub continue crescendo sem perder sua proposta principal: transformar a organização de uma casa compartilhada em algo mais simples, transparente e um pouco menos caótico.

