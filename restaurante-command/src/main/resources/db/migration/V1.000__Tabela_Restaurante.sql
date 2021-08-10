create table restaurante (
nome_fantasia character varying(100),
razao_social  character varying(100),
documento  character varying(30),
descricao  character varying(200)
);

create table forma_pagamento_restaurante (
id_restaurante numeric,
forma_pagamento  character varying(100)
);

create table horario_funcionamento_restaurante (
id_restaurante numeric(1,0),
hora_inicial  time,
hora_final  time
);
