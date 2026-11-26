# 🏡 RepHub

Trabalho final de POOWeb II, utilizando Spring Boot e API REST.
> Ana Júlia Bock Medina, SPI-UFSM


## Resumo

O **RepHub** organiza moradores, tarefas, avisos e registros financeiros de repúblicas e moradias compartilhadas, com objetivo de tornar a experiência de convivência mais simples e organizada.

## Escopo e arquitetura

Uma única aplicação Spring Boot, com API REST e banco PostgreSQL, organizada por funcionalidades: usuários; repúblicas e participações; convites; tarefas; avisos; financeiro.

| Componente | Responsabilidade |
| --- | --- |
| Entidade | Estado e comportamento do próprio objeto, como concluir uma atribuição ou ocultar um aviso |
| Service | Casos de uso, permissões, consultas, validações entre entidades e transações |
| Repository | Consulta e persistência |
| DTO | Dados recebidos e apresentados pela API, sem expor credenciais |
| Controller | Entrada e resposta HTTP, delegando os casos de uso aos services |

O [diagrama de classes revisado](Docs/diagrama_classes_melhorado.png) contém atributos, operações, associações, enumerações, services e DTOs.

## Tecnologias

Java, Spring Boot, Spring Web, Spring Data JPA, PostgreSQL, Flyway e Swagger/OpenAPI.

## Regras de negócios
disponível em: [Docs/regras_negocios.md](Docs/regras_negocios.md)

## Endpoints HTTP
disponível em:
[Docs/endpoints_http.md](Docs/endpoints_http.md)

## Documentação

- [Índice da documentação](Docs/README.md).
- [Diagrama de classes](Docs/diagrama_classes_melhorado.png).
- [Ontologia e modelagem conceitual](Docs/ontologia_modelagem_conceitual/): referência conceitual do trabalho
- [Diagrama inicial](Docs/diagrama_de_classes.png): versão anterior e preservada, mas não representa todas as decisões atuais.
- [Diagrama ER](Docs/diagrama_ER.png).

## Evolução futura

Ideias:
- Múltiplas participações simultaneamente ativas; 
- votações; 
- auditoria completa; 
- prioridades, pontuação e ranking de tarefas; 
- gamificação e Índice de Caos; 
- notificações; 
- calendário; i
- ntegrações financeiras e estornos; 
- frontend e dashboards;

O [planejamento de evolução](Docs/projeto_continuacao_futuro/desenvolvimento_fututo.md) e o [diagrama futuro](Docs/projeto_continuacao_futuro/diagrama_classe_futuro.png) são propostas independentes, não requisitos adicionais da versão acadêmica.
