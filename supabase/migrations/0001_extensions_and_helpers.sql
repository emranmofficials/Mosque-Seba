-- 0001_extensions_and_helpers.sql
-- Extensions only. The security-helper functions (current_profile_id,
-- current_mosque_id, has_permission) live in 0003_helper_functions.sql,
-- AFTER the tables they query (public.profiles etc.) are created in
-- 0002_core_identity_tenant.sql. Do not move them back here — that ordering
-- bug was caught by a local apply test (see supabase/README.md "Verifying").

create extension if not exists "uuid-ossp";
create extension if not exists pgcrypto;
