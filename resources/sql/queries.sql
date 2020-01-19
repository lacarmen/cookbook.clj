-- :name create-user! :<!
-- :doc creates a new active user record
insert into users
(id, first_name, last_name, email, pass, admin)
values (:id, :first-name, :last-name, :email, :pass, :admin)
returning id

-- :name update-user! :! :n
-- :doc updates an existing user record
update users
set first_name = :first-name,
    last_name = :last-name,
    email = :email,
    admin = :admin,
    active = :active
where id = :id

-- :name update-user-with-pass! :! :n
-- :doc updates an existing user record
update users
set first_name = :first-name, last-name = :last-name, email = :email, admin = :admin, pass = :pass
where id = :id

-- :name update-user-last-login! :! :n
update users
set last_login = now()
where id = :id

-- :name get-users :? :*
select id, first_name, last_name, active, admin, last_login
from users
where id <> :id

-- :name get-user-by-id :? :1
-- :doc retrieves a user record given the id
select * from users
where id = :id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
delete from users
where id = :id


-- :name get-tags :? :*
select tags from recipe

-- :name get-recipe-headers :? :*
select id, recipe->>'title' as title, recipe->>'description' as description, tags from recipe
where deleted is not true

-- :name get-recipe-by-id :? :1
select r.id, r.tags, r.recipe, u.first_name || ' ' || u.last_name as author, r.last_updated
from recipe r join users u on r.author = u.id
where r.id = :id

-- :name create-recipe! :<!
insert into recipe
(tags, recipe, author)
values (:tags, :recipe, :author)
returning id

-- :name update-recipe! :! :n
update recipe
set tags = :tags, recipe = :recipe, last_updated = now()
where id = :id

-- :name delete-recipe! :! :n
update recipe
set deleted = true
where id = :id
