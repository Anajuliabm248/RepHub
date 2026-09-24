# 🏡 RepHub

Trabalho final de POOWeb II, utilizando Spring Boot e API REST.
> Ana Júlia Bock Medina, SPI-UFSM

O **RepHub** organiza moradores, tarefas, avisos e registros financeiros de repúblicas e moradias compartilhadas. O sistema registra informações para acompanhamento da convivência e dos custos; não executa transferências, não constitui contrato imobiliário e não garante a cobrança de dívidas.

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

## 1. Usuários e contas

- Cada morador precisa ter sua própria conta. `Usuario` reúne dados pessoais e de acesso; não haverá uma entidade `Pessoa` separada nesta versão.
- Nome, e-mail e senha obrigatórios. E-mail válido e único, inclusive entre contas desativadas. Armazenar somente o hash da senha, nunca devolvido pela API.
- O usuário pode estar cadastrado sem participar de nenhuma república. Data de cadastro e identificadores são definidos pelo sistema.
- Somente o titular solicita a desativação da conta. Não há reativação nem exclusão definitiva.
- Desativação bloqueada por parcelas em aberto de despesas não canceladas ou pagamentos pendentes, em qualquer participação. Parcelas retiradas do rateio não geram saldo a cobrar.
- O último administrador ativo de uma república ativa deve transferir a administração ou encerrar a república antes de desativar a conta.
- Desativação permitida encerra a participação ativa e cancela suas atribuições pendentes na mesma operação. Registros anteriores são preservados.
- Conta desativada não pode autenticar-se nem consultar registros. Seu e-mail continua reservado.
- Limites de tamanho dos campos e política de senha serão detalhados nas validações da implementação; não se pressupõem limites numéricos já aprovados.

## 2. Repúblicas, participação e administração

### Vínculo e papéis

- República tem nome obrigatório, descrição opcional e pode ter o mesmo nome de outra república.
- Criar uma república cria também a participação `ADMIN` do fundador, em uma única transação.
- Cada participação liga um usuário a uma república, com papel, situação, entrada e saída. Um usuário pode ter várias participações históricas, mas **no máximo uma ativa**.
- Ingresso por convite cria participação `MORADOR`.
- Cada retorno, inclusive à mesma república, cria uma nova participação. Participações encerradas nunca são reativadas.
- República ativa mantém pelo menos um administrador ativo. Pode haver vários.
- Somente administradores ativos da mesma república editam seus dados, promovem participantes ativos, alteram seus papéis e removem moradores, inclusive outros administradores. Não podem retirar o último administrador ativo enquanto a república permanecer ativa; papéis de vínculos encerrados não são modificados.
- Saída voluntária e remoção encerram o vínculo e cancelam atribuições pendentes. Atribuições concluídas e registros financeiros permanecem.
- Dívidas não impedem saída ou remoção. Continuam vinculadas à participação original; um novo ingresso não as transfere para o novo vínculo.

### Encerramento da república

- Qualquer administrador ativo pode encerrar a república, inclusive com dívidas ou pagamentos pendentes. O sistema informa essas pendências antes da ação, **sem bloquear o encerramento**.
- Encerramento desativa a república, encerra participações ativas, cancela convites ainda utilizáveis e cancela atribuições pendentes. Convites expirados conservam sua condição calculada de expirados.
- Não há reabertura. Depois do encerramento, nenhuma operação ou consulta daquela república fica disponível pela aplicação.
- Registros financeiros permanecem armazenados como estavam: encerrar não quita dívidas, não confirma pagamentos e não apaga registros.
- O bloqueio da república encerrada prevalece sobre as permissões de ex-moradores. Não será possível pagar ou confirmar registros antigos nesse contexto.
- Consequência das regras escolhidas: dívidas ou pagamentos pendentes preservados em uma república encerrada continuam bloqueando a desativação da conta do devedor, sem possibilidade de regularização pela aplicação nesta versão. O aviso de encerramento deve explicitar esse efeito.

### Acesso de ex-moradores

- Enquanto conta e república estiverem ativas, quem saiu acessa apenas seu próprio financeiro daquela participação: consulta extrato, registra pagamentos, edita ou cancela seus pagamentos pendentes.
- Não recebe novas parcelas no vínculo encerrado nem consulta avisos, tarefas ou financeiro coletivo da antiga república.
- Administradores atualmente ativos confirmam os pagamentos de ex-moradores.

## 3. Convites

- Somente administradores ativos emitem convites para sua república. Não há limite de quantidade.
- Código único, sem destinatário previamente definido e de uso único. Não exige e-mail convidado.
- Todo convite expira exatamente **48 horas após a criação**, sem configuração. No instante da expiração, já não pode ser usado.
- Uso exige conta ativa, república ativa e ausência de participação ativa do usuário.
- Estados persistidos: `DISPONIVEL`, `UTILIZADO`, `CANCELADO`. Expiração é calculada; um disponível com prazo encerrado é apresentado como expirado.
- Qualquer administrador ativo da república cancela convite disponível e não expirado. Utilizado não pode ser cancelado; expirado permanece expirado.
- Convite continua válido se seu emissor sair ou perder o papel administrativo, respeitadas as demais condições.
- Registrar emissor, criação, expiração, utilização e, quando houver, autor e data do cancelamento. O utilizado referencia a participação originada.
- Consumir código e criar participação constituem uma única operação, protegida contra uso simultâneo do mesmo código por duas pessoas.

## 4. Tarefas e atribuições individuais

### Criação e responsabilidade

- Qualquer participante ativo cria e consulta tarefas da própria república.
- Título obrigatório; descrição e prazo opcionais. Atraso é calculado a partir do prazo, sem estado persistido `ATRASADA`.
- Uma tarefa pode nascer sem responsáveis e ter várias pessoas atribuídas depois.
- `AtribuicaoTarefa` liga tarefa e participação responsável e registra autor da atribuição, data, situação e datas de conclusão ou cancelamento.
- Qualquer participante ativo pode atribuir a si ou a outros participantes ativos da mesma república.
- Estados da atribuição: `PENDENTE`, `CONCLUIDA`, `CANCELADA`.
- Não pode haver duas atribuições não canceladas da mesma tarefa à mesma participação. Nova atribuição é permitida após cancelamento da anterior, preservando o registro anterior.
- Trocar responsável significa cancelar a atribuição anterior e criar outra. Responsável, autor da atribuição ou administrador ativo cancela uma atribuição pendente. Concluídas não são canceladas diretamente; o responsável pode primeiro desfazer sua conclusão.

### Conclusão e edição

- Somente o responsável, com participação ativa, marca ou desmarca sua conclusão, como checkbox. Desmarcar limpa a data de conclusão e pode reabrir a tarefa.
- Criador ativo ou administrador ativo edita título, descrição e prazo de tarefa pendente. Enquanto concluída, não permite edição desses dados nem novas atribuições.
- Nova ocorrência de uma atividade concluída exige outra tarefa. Desmarcar serve para corrigir a conclusão da mesma ocorrência.
- Situação geral calculada: sem atribuições ou com todas canceladas, `PENDENTE`; com alguma pendente, `PENDENTE`; com pelo menos uma concluída e nenhuma pendente, `CONCLUIDA`.
- Canceladas são ignoradas no cálculo. Se Ana concluiu e a atribuição pendente de Bruno foi cancelada, a tarefa passa a concluída.
- Saída cancela automaticamente as atribuições pendentes do morador e preserva as concluídas.

### Cancelamento da tarefa

- Criador ativo ou administrador ativo pode cancelar a tarefa. **Atribuições concluídas devem ser preservadas**, conforme a revisão da regra de exclusão.
- Para manter essas relações, adota-se cancelamento lógico da tarefa, sem apagar a tarefa ou suas atribuições. A ação cancela as pendentes e mantém as concluídas.
- Registrar autor e data do cancelamento. Tarefa cancelada fica fora da lista de tarefas em andamento, não recebe atribuições, não permite edição nem alterações nos checkboxes.
- O cancelamento é definitivo nesta versão. Sua situação geral é `CANCELADA`, com precedência sobre o cálculo das atribuições.

## 5. Quadro de avisos

- Qualquer participante ativo publica e consulta avisos da própria república. Título e mensagem obrigatórios.
- Aviso tem uma república, uma participação autora e data de publicação. Somente autor ativo edita aviso visível; registrar a data da última edição.
- Autor ativo ou administrador ativo pode ocultar e restaurar um aviso.
- Ocultar não apaga conteúdo ou autoria. Registrar autor e data da remoção; data de remoção preenchida indica ocultação.
- Ocultos não são exibidos no quadro nem podem ser editados. Não haverá listagem pública ou conteúdo consultável de avisos ocultos. O controle de restauração por identificador fica restrito a autor e administradores autorizados.
- Restaurar limpa a data e a referência de remoção atuais. Sem histórico de ciclos de ocultação/restauração nesta versão.
- Saída do autor não remove seus avisos; continuam visíveis até eventual ocultação por administrador.

## 6. Despesas e parcelas

### Cadastro e rateio

- Qualquer participante ativo cadastra despesas e consulta despesas e parcelas da própria república.
- `Despesa` representa o gasto coletivo. `ParcelaDespesa` é a parte devida por uma participação, não um parcelamento mensal.
- Despesa nasce junto com a divisão e pelo menos uma parcela incluída. Tudo é salvo na mesma transação.
- Título, categoria, referência, valor total e tipo de divisão obrigatórios. Descrição e vencimento opcionais.
- Referência futura não é aceita. Vencimento pode ser anterior à referência. Criação é definida pelo sistema.
- Categorias: aluguel, água, energia, internet, mercado, limpeza, manutenção, lazer e outros.
- Criador pode ficar fora do rateio. Novas parcelas exigem participações ativas da mesma república.
- No máximo uma parcela por par despesa–participação. Soma das parcelas incluídas igual ao total.
- `IGUAL`: calcular em centavos e distribuir o restante por identificador crescente da participação. Exemplo: R$ 100 para três pessoas gera R$ 33,34, R$ 33,33 e R$ 33,33.
- `PERSONALIZADA`: informar valores dos participantes selecionados; quem não contribuir fica fora do rateio.
- **Mínimo de R$ 1 apenas para o total da despesa.** Parcelas e pagamentos podem ser inferiores a R$ 1, mas devem ser positivos e representáveis em centavos.
- Nenhuma parcela incluída pode ser zero. Rejeitar rateio igual quando o total em centavos for menor que a quantidade de participantes, em vez de criar parcelas zeradas.

### Alteração

- Criador ativo ou administrador ativo altera a despesa. Qualquer pagamento confirmado em qualquer parcela bloqueia **toda edição**, inclusive descrição, referência e vencimento.
- Pendente bloqueia alterações financeiras: total, participantes, valores individuais e tipo de divisão. Demais dados podem ser editados se não houver confirmado.
- Cancelados são preservados, mas não bloqueiam alterações.
- Como política de preservação adotada nesta revisão, parcelas de participações encerradas mantêm valores e destinatários originais em alterações posteriores do rateio. Demais alterações devem respeitar essas obrigações e a soma total; novo rateio igual que as modifique é rejeitado.
- Retirar parcela de participante ativo do rateio não apaga pagamentos cancelados: ela passa a não incluída. Não integra custo ou saldo e não recebe pagamentos. Pode ser reincluída, se a participação estiver ativa e as demais condições permitirem.
- As restrições consideram pagamentos de todas as parcelas, inclusive as retiradas do rateio.

### Cancelamento

- Criador ativo ou administrador ativo cancela somente sem pagamento confirmado em qualquer parcela.
- Cancelar também cancela todos os pagamentos pendentes e registra autor e data, na mesma transação.
- Cancelamento definitivo: sem restauração, edição, novos pagamentos ou saldo a cobrar. Registros preservados.
- Valor pago, saldo e situação da parcela calculados. Situações: `PENDENTE`, `PARCIALMENTE_PAGA`, `PAGA`, `CANCELADA`; esta última decorre de despesa cancelada ou parcela fora do rateio.

## 7. Pagamentos

- Registros de contribuição à república ou reembolso, conforme sua organização. A aplicação não movimenta dinheiro nem controla crédito do recebedor ou compensações entre moradores.
- Para explicitar o contexto, propõe-se registrar finalidade `CONTRIBUICAO` ou `REEMBOLSO` e identificação textual do destinatário. Ele não precisa ter conta e não constitui outra entidade financeira nesta versão.
- Somente o devedor registra, inicialmente `PENDENTE`, inclusive para participação encerrada em república ainda ativa.
- Pagamentos parciais aceitos. Valor positivo, duas casas decimais e não superior ao saldo confirmado da parcela.
- **Simplificação adotada:** pendentes não reservam saldo. Vários pendentes podem, juntos, exceder o saldo; cada confirmação o revalida. Disponibilidade definida apenas pelos confirmados.
- Devedor pode editar os dados de um pendente, com nova validação. Não pode transferi-lo para outra parcela: cancelar e registrar outro nesse caso.
- Data de pagamento informada pelo morador, podendo ser retroativa. Como critério adotado nesta revisão, data futura não é aceita. Data de registro é automática e independente.
- Formas: PIX, dinheiro, transferência, cartão e outro. Observação opcional.
- Administrador ativo da mesma república confirma, inclusive o próprio pagamento. Registrar quem confirmou e quando.
- Só confirmados reduzem saldo. Soma confirmada nunca supera o devido, inclusive em confirmações simultâneas.
- Devedor ou administrador ativo cancela pendente, com motivo obrigatório, autor e data. Cancelamento automático por despesa usa autor dessa ação e motivo correspondente.
- `CONFIRMADO` e `CANCELADO` são finais. Confirmados não podem ser editados ou cancelados. Correções ou estornos após confirmação ficam fora desta versão.

## 8. Extrato financeiro e indicadores

### Acesso e contexto

- Extrato é consulta calculada retornada por DTO, sem tabela que duplique movimentações.
- Somente titular consulta seu extrato individual. Ser administrador não permite consultar extrato individual alheio.
- Participantes ativos consultam também extrato coletivo da república; isso não concede acesso ao extrato pessoal de outro usuário.
- Individual permite selecionar uma participação ou reunir participações do mesmo usuário na mesma república, sem duplicar despesas no total coletivo.
- Ex-morador conserva apenas o próprio financeiro, enquanto conta e república ativas.
- Totais e categorias coletivos não são retornados ao ex-morador. A visão coletiva também não retorna a lista de identificadores das participações selecionadas internamente nem extratos individuais alheios.
- “Todo o período” considera os períodos de participação selecionados. Pagamentos posteriores à saída continuam consultáveis por seleção explícita do mês, enquanto a república estiver ativa.

### Valores e datas

| Informação | Critério |
| --- | --- |
| Custo individual do período | Parcelas incluídas de despesas não canceladas com referência no período |
| Custo coletivo do período | Total das despesas não canceladas com referência no período, contado uma vez por despesa |
| Pagamentos do período | Confirmados com data de pagamento no período |
| Saldo atual das parcelas do período | Devido das parcelas selecionadas menos todos os seus pagamentos confirmados, sem limitar estes últimos ao período |

- Custo de agosto pago em setembro aparece como custo em agosto e pagamento em setembro.
- Saldo atual não é simplesmente custo menos pagamentos do período. Não há reconstrução do saldo histórico no último dia do período.
- Despesas canceladas e parcelas retiradas não integram seus custos e saldos correspondentes. Pagamentos pendentes e cancelados não entram no total pago.
- Consultas usam meses de calendário: mês, intervalo de meses, ano ou todo o período. Mês inicial incluído; limite final exclusivo é o início do mês seguinte ao último selecionado.
- Não aceitar mês futuro. Ano atual abrange até o mês atual. Mês atual aparece como em andamento, com registros existentes até a consulta.
- “Todo o período” abrange do mês de entrada ao mês de saída de cada participação, ou ao mês atual se ativa. Na visão agregada, usa-se a união desses meses sem duplicações.
- No extrato coletivo, “todo o período” vai do mês de criação da república ao mês atual. Para uma seleção de N meses, a comparação considera os N meses imediatamente anteriores ao primeiro selecionado.

### Demais regras de négocios

- Série mensal de custo, pagamentos e saldo atual das parcelas daquele mês.
- Meses sem custo aparecem como zero e entram no menor custo, mas ficam fora do divisor da média mensal. Sem meses com custo, média zero.
- Maior e menor custo mensal, total gasto pela república e categorias individuais e coletivas. Total zero gera percentual por categoria zero.
- Comparação com período imediatamente anterior de mesma duração. Períodos sem custos tratados como zero.
- Diferença absoluta e tendência de aumento, diminuição ou estabilidade. Base anterior zero torna a variação percentual indisponível, nunca infinita ou artificialmente zero.
- Indicadores refletem registros atuais, inclusive confirmações posteriores e cancelamentos permitidos. Não preservam versões anteriores dos relatórios.
- Convivência exige conta ativa, república ativa e participação ativa na mesma república. Exceções financeiras de ex-moradores estão definidas acima.
- Services verificam identidade autenticada, papel e autoria, não confiam nessas informações enviadas pelo cliente.
- Preservar usuários, repúblicas, participações, convites, despesas, parcelas, pagamentos e avisos. Cancelamento lógico de tarefas preserva também suas atribuições concluídas.
- Manter estado atual e datas/autores relevantes, sem trilha completa de alterações.
- Apresentação no fuso `America/Sao_Paulo`. Instantes representados por `Instant` no diagrama e preservados em UTC; referência, vencimento e data informada de pagamento usam `LocalDate`.
- Valores em `BigDecimal`, duas casas decimais, sem `float` ou `double`. Rateio igual usa centavos inteiros.
- Services coordenam atomicamente criação de república/fundador, ingresso por convite; saída/atribuições; desativação de conta; encerramento de república; criação/alteração de rateio; cancelamento de despesa/pendentes; confirmação de pagamento.
- Controlar concorrência: uma participação ativa; uso único do convite; último administrador; alterações de rateio versus pagamentos; soma de confirmados. Uma validação isolada antes da gravação não basta.

## Tecnologias

Java, Spring Boot, Spring Web, Spring Data JPA, PostgreSQL, Flyway e Swagger/OpenAPI.

## Documentação

- [Índice da documentação](Docs/README.md).
- [Diagrama de clsses](Docs/diagrama_classes_melhorado.png).
- [Ontologia e modelagem conceitual](Docs/ontologia_modelagem_conceitual/): referência conceitual anterior (divergências do produto são resolvidas pelas regras acadêmicas deste README)
- [Diagrama inicial](Docs/diagrama_de_classes.png): versão anterior e preservada, mas não representa todas as decisões atuais.
- [Diagrama ER](Docs/diagrama_ER.png).

## Evolução futura

Múltiplas participações simultaneamente ativas; votações; auditoria completa; prioridades, pontuação e ranking de tarefas; gamificação e Índice de Caos; notificações; calendário; integrações financeiras e estornos; frontend e dashboards avançados; docker.

O [planejamento de evolução](Docs/projeto_continuacao_futuro/desenvolvimento_fututo.md) e o [diagrama futuro](Docs/projeto_continuacao_futuro/diagrama_classe_futuro.png) são propostas independentes, não requisitos adicionais da versão acadêmica.
