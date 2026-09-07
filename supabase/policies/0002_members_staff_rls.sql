-- 0002_members_staff_rls.sql
-- RLS for members, member_preferences, staff, staff_availability.

alter table public.members enable row level security;
alter table public.member_preferences enable row level security;
alter table public.staff enable row level security;
alter table public.staff_availability enable row level security;

-- members
create policy members_select_same_mosque on public.members
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.view'));

create policy members_select_self on public.members
  for select
  using (profile_id = public.current_profile_id());

create policy members_insert_with_permission on public.members
  for insert
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.create'));

create policy members_update_with_permission on public.members
  for update
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.update'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('member.update'));

-- Archival only, never a hard delete from the client (docs/DATABASE_ARCHITECTURE.md).
create policy members_archive_with_permission on public.members
  for update
  using (mosque_id = public.current_mosque_id() and public.has_permission('member.archive'))
  with check (mosque_id = public.current_mosque_id());

-- member_preferences: owner or same-mosque admin with member.view
create policy member_preferences_select on public.member_preferences
  for select
  using (
    mosque_id = public.current_mosque_id()
    and (
      public.has_permission('member.view')
      or member_id in (select id from public.members where profile_id = public.current_profile_id())
    )
  );

create policy member_preferences_update_self on public.member_preferences
  for update
  using (member_id in (select id from public.members where profile_id = public.current_profile_id()))
  with check (member_id in (select id from public.members where profile_id = public.current_profile_id()));

-- staff: general visibility via staff.view; salary columns are additionally
-- gated at the application/RPC layer by staff.salary.view (RLS alone cannot
-- hide individual columns — see docs/SECURITY_MODEL.md for the RPC pattern
-- used to serve a salary-redacted view to holders of staff.view only).
create policy staff_select_same_mosque on public.staff
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.view'));

create policy staff_write_with_permission on public.staff
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'));

create policy staff_availability_select on public.staff_availability
  for select
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.view'));

create policy staff_availability_write on public.staff_availability
  for all
  using (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'))
  with check (mosque_id = public.current_mosque_id() and public.has_permission('staff.manage'));

comment on policy staff_select_same_mosque on public.staff is
  'RLS grants row visibility only. Salary amount must additionally be redacted at the RPC layer unless the caller also holds staff.salary.view.';
