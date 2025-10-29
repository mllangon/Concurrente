-- Sensores demo
insert into sensor (id, name, type, location, active, created_at) values
  (random_uuid(), 'Motion-Lobby', 'MOTION', 'Lobby', true, CURRENT_TIMESTAMP()),
  (random_uuid(), 'Temp-ServerRoom', 'TEMPERATURE', 'Server Room', true, CURRENT_TIMESTAMP()),
  (random_uuid(), 'Access-GateA', 'ACCESS', 'Gate A', true, CURRENT_TIMESTAMP());

-- Accesos demo
insert into access_log (id, person_id, person_name, authorized, location, ts) values
  (random_uuid(), 'P001', 'Tony Stark', true, 'Gate A', CURRENT_TIMESTAMP()),
  (random_uuid(), 'P002', 'Pepper Potts', true, 'Gate A', CURRENT_TIMESTAMP());


