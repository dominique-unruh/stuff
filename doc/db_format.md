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
* Do not start with `-` or `.`
* Contain only characters `A-Za-z0-9_@,.-` (these characters have been chosen to be shell-friendly)

Example:
* `items/`
  * `1234.yaml` (references an image `test.jpg`)
  * `5678.yaml`
* `files/`
  * `1234`
    * `test.jpg`

## Item files

Each item has a corresponding item file `id.yaml` in `items/` where `id` is the id of the item in decimal.

The content of the file is YAML with the following supported tags:
* `id` (integer, mandatory): The id of the item (`item.id`)
* `name` (string, mandatory): The name of the item as plain text (`item.name`)
* `description` (string, optional): The description of the item as HTML.
  The description can contain extended item URLs (see below) but not extended file URLs.
* `photos` (list of URLs, optional): The photos of the item (first one is the main photo) (`item.photos`)
  * Each entry is an extended URL (see below).
  * If the URL is local (see below), it must reference to an existing file in the `files/` directory with the right id.
* `codes` (list of strings, optional):
  QR or barcodes associated with this item.
  * Each barcode is represented by a string of the form `type:content`.
  * `type` represents the type of barcode (e.g., `QR_CODE`). For a full list of types see [here](https://github.com/mebjas/html5-qrcode/#supported-code-formats).
  * `type` can be `UNKNOWN` to represent an unknown format
  * `content` is the actual content of the QR/barcode
  * UTF-8 is used to encode non-ASCII `content` (if the barcode format supports this)
  * Binary `content` is encoded as base64.
* `files` (list of URLs, optional, not implemented): 
  Additional attached files (e.g., manuals).
  * Each string must be a local URL referencing an existing file in the `files/` directory with the right id.
* `lastaccessed` (integer, optional, not implemented): Time of last access (last opened in full screen view), in seconds since epoch
* `lastmodified` (integer, optional): Time of last modification, in seconds since epoch.
  Does not include automatic modifications such as, e.g., setting `lastaccessed`.
* `location` (integer, optional, not implemented): Id of an existing item that contains this item. 
* `prevlocation` (integer, optional, not implemented): Previous value of `location` (for UI suggestions when setting `location`). 

For all fields that are lists of something (e.g., `photos`, `links`), empty lists are disallowed.
(But the whole field can be omitted if it is optional.)

### Extended URLs

An extended URL is either a normal URL (referencing an external resource or a data URL,
interpretation will be left to the browser), or a *local URL*.

A *local URL* references a file in the `files` directory.
It is of the form `localstuff:id/file`.
Here `id` is the id of an item (and an item must only reference local file URLs with its own ID).
And `file` is the name of a file in the directory `files/id`.

A *local URL* can also reference an item itself.
Then it is of the fom `localstuff:id`.
Here `id` is the id of an item.
Items are allowed to reference other items via local item URLs.
