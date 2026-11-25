-- Synthetic data for stg only (loaded by `avk seed pingpong`). Safe to run more than once.
insert into greeting (message)
select 'pingpong from the stg seed' where not exists (select 1 from greeting where message = 'pingpong from the stg seed');
