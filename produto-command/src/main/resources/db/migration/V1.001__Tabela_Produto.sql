create table produto (
id bigint not null primary key,
id_restaurante bigint not null,
nome character varying(100) not null,
descricao character varying(100),
valor numeric not null
);

alter table produto add constraint produto_restaurante_fk foreign key (id_restaurante) references restaurante (id);

CREATE SEQUENCE produto_id INCREMENT 1 START 1;
