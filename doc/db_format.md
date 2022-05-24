# Database format

## Overall structure

This section describes the structure of a single user DB

Directories:
* `items` - the YAML files of the actual items (one per item)
* `files` - associated files (e.g., photos)
* Any additional directories or files on that level are ignored (e.g., you can have a `.git` directory)

Each registered item in the DB has a single file YAML file named `id.yaml` in `item`.
Here `id` is the unique ID of the item (formatted as a decimal integer).

The `files` directory contain subdirectories named `id`, corresponding to items with id `id`.
An item is only allowed to reference files in the subdirectory with the same ID.
Inside this directory, arbitrary file names are allowed, as long they satisfy the following conditions:
* At least one character long
* Do not start with `-`
* Contain only characters `a-z0-9_@,.-` (these characters have been chosen to be shell-friendly)

Example:
* `items/`
  * `1234.yaml` (references an image `test.png`)
  * `5678.yaml`
* `files/`
  * `1234`
    * `test.png`

## Item files

Each item has a corresponding item file `id.yaml` in `items/` where `id` is the id of the item in decimal.

The content of the file is YAML with the following supported tags:
* `id` (integer, mandatory): The id of the item (`item.id`)
* `name` (string, mandatory): The name of the item as plain text (`item.name`)
* `description` (string, optional): The description of the item as HTML
* `photos` (list of strings, optional): The photos of the item (first one is the main photo) (`item.photos`)
  * Each entry is an extended URL (see below).
  * If the URL is local (see below), it must reference to an existing file in the `files/` directory with the right id.
  * Must be nonempty (if there are no photos, this tag is omitted)
* `codes` (list of strings, optional, not implemented):
  QR or barcodes associated with this item.
  * Each barcode is represented by a string
  * UTF-8 is used to encode non-ASCII strings (if the barcode format supports this)
  * Binary QR-codes are encoded as base64.
  * Must be nonempty (if there are no tags, this tag is omitted)
* `files` (list of strings, optional, not implemented): 
  Additional attached files (e.g., manuals). The same conditions as with `photos` apply.
* `links` (list of strings, optional, not implemented): Links to the web with further information (e.g., product pages)

### Extended URLs

An extended URL is either a normal URL (referencing an external resource or a data URL,
interpretation will be left to the browser), or a *local URL*.

A *local URL* references a file in the `files` directory.
It is of the form `localstuff:id/file`.
Here `id` is the id of an item (and an item must only reference local URL with its own ID).
And `file` is the name of a file in the directory `files/id`.

