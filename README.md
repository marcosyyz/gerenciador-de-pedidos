# Gerenciador de Pedidos

Sistema para gerenciar e calcular pedidos usando Java Spring Boot, Mysql e integração Kafka,
utilizando maven para controle de dependencias, spring-data para facilitar conexoes de banco de dados.

## Sobre o projeto

O sistema recebe pedidos de um sistema externo A, calcula os valores dos produtos e disponibiliza para um sistema externo B.
Foi desenvolvido para suportar alto volume de transações (150-200k pedidos por dia).

## Stack tecnológica

- Java 17
- Spring Boot
- MySQL 
- Apache Kafka
- Docker

## Como funciona

1. Sistema A envia pedidos para tópico Kafka, topico: "orders-received"
2. Aplicação consome os pedidos, calcula valores e salva no banco
3. Aplicação publica pedidos calculados no tópico "orders-processed" 
4. Sistema B consome os pedidos já processados

## Estrutura do banco

```sql
orders - pedidos principais
order_items - itens de cada pedido  
order_logs - logs do processamento
```

## Para executar

Copie o arquivo .env.example para .env e configure as senhas.

```
docker-compose up
```

Isto vai subir MySQL, Kafka e a aplicação.

## Testes

Há alguns testes unitários cobrindo:
- Processamento normal de pedidos
- Verificação de duplicados
- Busca de pedidos processados
- Validações básicas

Execute com: mvn test

## Desenvolvimento local

Para rodar só a aplicação local: mvn spring-boot:run

ou para rodar kafka, mysql e o app junto no docker: 

docker-compose build
docker-compose up -d






## Verificar se está funcionando

docker-compose logs order-service
docker-compose logs kafka


Para ver mensagens nos tópicos Kafka use os comandos de console do próprio Kafka.

``` simular envio de mensagem usando kafka-console-producer```
echo {"externalOrderId":"EXT-0001-2025","orderNumber":"ORD-NOVO-001","items":[{"productName":"Cerveja Nova 2025","quantity":6,"unitPrice":5.50}]} | docker exec -i order-kafka kafka-console-producer --bootstrap-server kafka:9092 --topic orders-received


``` simular consumo de mensagem usando kafka-console-consumer para ver o order ja processado no topico orders-processed```
docker exec order-kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic orders-processed --from-beginning

``` acessar container mysql e executar queries ```
docker-compose exec mysql bash
mysql -u root -p