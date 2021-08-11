create table restaurante (
id numeric not null primary key,
nome_fantasia character varying(100),
razao_social  character varying(100),
documento  character varying(30),
descricao  character varying(200)
);

create table forma_pagamento_restaurante (
id_restaurante numeric,
forma_pagamento  character varying(100)
);

alter table forma_pagamento_restaurante add constraint forma_pagamento_restaurante_fk foreign key (id_restaurante) references restaurante (id);

create table horario_funcionamento_restaurante (
id_restaurante numeric,
hora_inicial  time,
hora_final  time
);

alter table horario_funcionamento_restaurante add constraint horario_restaurante_fk foreign key (id_restaurante) references restaurante (id);

CREATE SEQUENCE restaurante_id INCREMENT 1 START 1;
