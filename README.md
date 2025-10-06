# Ivi Invest – API (Spring Boot, Java 17)
Fornece autenticação com 2FA por e-mail, recuperação de senha, perfil de investidor, fluxo de objetivos e carteiras (percentuais, ativos, simulação), além de um endpoint de chat para perguntas sobre a carteira.

| Nome | RM |
|------|-----|
| Henrique Pontes Oliveira | RM98036 |
| Levy Nascimento Junior | RM98655 |
| Rafael Autieri dos Anjos | RM550885 |
| Rafael Carvalho Mattos | RM99874 |
| Vinicius Santos Yamashita de Farias | rm550885 |

## Estrutura

| Nome | RM |
|------|-----|
| Henrique Pontes Oliveira | RM98036 |
| Levy Nascimento Junior | RM98655 |
| Rafael Autieri dos Anjos | RM550885 |
| Rafael Carvalho Mattos | RM99874 |
| Vinicius Santos Yamashita de Farias | rm550885 |

Front-end é feito em REACT-NATIVE, [Link do projeto](https://github.com/Pontessxx/IviInvest) temos telas já prontas com figma:
*[API - FACE RECOG](https://github.com/Pontessxx/API_FACERECOG_IVINVEST)*
![img](https://github.com/Pontessxx/Api_iviinvest/blob/master/figma_img.png)

## Endpoints (overview)
![img](https://github.com/Pontessxx/Api_iviinvest/blob/master/insomnia.png)
#### Autenticação & 2FA
- `POST` /api/v1/auth/login – login (etapa 1, sem JWT ainda)
- `POST` /api/v1/auth/2fa/send – envia código 2FA por e-mail
- `POST` /api/v1/auth/2fa/verify – verifica 2FA e retorna JWT
#### Usuários
- `GET` /api/v1/auth – listar usuários (admin/dev)
- `GET` /api/v1/auth/{id} – buscar por ID
- `PUT` /api/v1/auth/{id} – atualizar usuário
- `DELETE` /api/v1/auth – excluir usuário por token autenticado
- `POST` /api/v1/auth/register – cadastro de novo usuário
- `GET` /api/v1/auth/perfil – obter perfil de investidor (JWT)
- `PUT` /api/v1/auth/perfil – atualizar perfil de investidor (JWT)
#### Recuperação de senha
- `POST` /api/v1/recover/token – gerar e enviar token de recuperação
- `PUT` /api/v1/recover/password – redefinir senha com token
#### Objetivos do usuário
- `GET` /api/v1/objetivos – buscar último objetivo (JWT)
- `POST` /api/v1/objetivos – salvar objetivo (JWT)
- `GET` /api/v1/objetivos/historico – histórico completo (JWT)
#### Carteiras
- `POST` /api/v1/carteiras/percentuais/gerar – gerar duas carteiras (ex.: conservadora e agressiva) com percentuais (JWT)
- `POST` /api/v1/carteiras/selecionar – selecionar e salvar a carteira escolhida (JWT)
- `POST` /api/v1/carteiras/ativos/gerar – gerar ativos com base na carteira escolhida (JWT)
- `GET` /api/v1/carteiras/simulacao – simular rentabilidade ao longo do tempo (JWT)
- `GET` /api/v1/carteiras/selecionada – buscar carteira selecionada (JWT)
#### Chat
- `POST` /api/v1/carteiras/chat – perguntas sobre a carteira (JWT)
#### Health
- `GET` /api/v1/health – status da API
## Como rodar (dev)
Pré-requisitos:
- Java 17
- Maven 3.9+
- (opcional) Docker para subir DB externo
```
# 1) Clone
git clone https://github.com/Pontessxx/Api_iviinvest.git
cd Api_iviinvest

# 2) Configure variáveis (dev) em src/main/resources/application.properties (ou application-dev.properties)

# 3) Rodar migrações automaticamente e iniciar API
mvn spring-boot:run
# ou
mvn clean package && java -jar target/*.jar

```
### Documentação Swagger

Swagger UI: http://localhost:8080/swagger-ui/index.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
