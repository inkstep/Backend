# Inkstep Server Backend
Backend server for inkstep


[![CircleCI](https://circleci.com/gh/inkstep/backend.svg?style=svg)](https://circleci.com/gh/inkstep/backend)
![](https://img.shields.io/badge/database-postgres-purple.svg)
![](https://img.shields.io/badge/dependancies-gradle-green.svg)
![](https://img.shields.io/badge/container-docker-blue.svg)
![](https://img.shields.io/badge/server-aws-yellow.svg)
![](https://img.shields.io/badge/project-inkstep-black.svg)

url for HTTP requests

<http://inkstep.hails.info/>

# Scripts
Run ./install to install necessary programs

Run ./clean to clean the directory

Run ./run to run the server

## Local Install
```sh
sudo apt install mysql-server
sudo mysql_secure_installation utility
sudo systemctl start mysql
# Be warned Ubuntu uses `auth_socket` by default
```

# API Endpoints

-
## /journey

#### `GET /journey/:id`

Returns details of the journey corresponding to :id.

#### `GET /journey/:id/images`

Returns the images for the journey corresponding to :id.

#### `PUT /journey`

Creates a new journey object. Returns an empty json {}

#### JSON params

| Param | Description |
| ---- | ------ |
| `user_id` | The user id |
| `artist_id` | The id of the artist |
| `tattoo_desc` | What the tattoo will be of |
| `size` | Size of the tattoo |
| `position` | Position on the body of the tattoo |
| `availability` | The availability of the user |
| `deposit` | Whether the user is willing to put down a deposit |
| `ref_images` | The number of reference images needed |

### `PUT /journey/image`

Puts the given base64encode image in to the database

#### JSON params

| Param | Description |
| ---- | ------ |
| `journey_id` | The journey id |
| `image_data` | The base64Encoded image |


#### JSON params

| Param | Description |
| ---- | ------ |
| `user_name` | The username |
| `user_email` | The email |

### `GET /user/:id`

Retrieves the user information for the corresponding user id.

## /artist

### `GET /artist/:id`

Retrieves the artist information for the corresponding user id.
