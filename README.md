# sbbs

Simple bulletin board system.

## Usage
See your system administrator to set you up as a member of the
appropriate groups. Run `sbbs` to access the system.

## History
During the first generation of [hackNET](http://www.hack-net.org), the
group was oriented around information security. The current crop of
forums were deemed too heavy and insecure (many vulnerabilities had
cropped up with several of the PHP-based forums), so the group decided
to write a console-based forum software in Perl called `sbbs`, or 
"secure bulletin board system".

The initial idea was to use ncurses (or perhaps just a bare stdio)
interface that ran on login; i.e. the forum would run on a dedicated
machine (this was before virtual machines became very accessible) and
the users' login shell would be replaced by the `sbbs` binary. `sbbs`
was never finished due to a combination of life's vagaries and a
general lack of developing skill among the group's members at that time.

During discussion on the hackNET [SILC](http://www.silc.org) channel, 
[steveo](http://saolsen.github.com) brought up sbbs and noted, "now,
it would take a few days of solid hacking to finish." I was looking
for a project to get back into Clojure with, so I've taken up work on
sbbs. Now, the `s` in `sbbs` stands for "simple", but it is still
built with security in mind.

### Why the change from 'secure' to 'simple'
Given the design of the current database, i.e. a console application running
on a hackNET-controlled server where only hackNET members have access, and
all access is via SSH, it has been deemed to have enough security from the
environment it is deployed on. The focus now is on a simple system to facilitate
getting projects together.

## Core Principles

### Leverage the underlying UNIX implementation for the hard work
Where possible, **sbbs** uses UNIX to accomplish its tasks. For
example, users in the `sbbs` group are allowed access to the `sbbs`
software. Access to the subcategories is determined by checking for
membership in the appropriate group (sbbs-<category-name>).

## Board Layout
The board is subdivided into categories. These are intended to be high-level
organisational units for conversations. Conversations, in turn, are termed
"threads" and are composed of a "parent" comment and a number of replies,
organised by the timestamp they are posted. In the code, a lot of references 
are made to comment and category IDs; these are the UUIDs assigned by couch
to the document representing a category or a comment.

## License

Copyright 2012 Kyle Isom <coder@kyleisom.net>. 
Distributed under the ISC license.

## Powered by
* Clojure, Apache CouchDB, Emacs, Slime, Swank

"Hacks and glory await!" - SLIME
