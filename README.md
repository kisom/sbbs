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

## Core Principles

### Leverage the underlying UNIX implementation for the hard work
Where possible, **sbbs** uses UNIX to accomplish its tasks. For
example, users in the `sbbs` group are allowed access to the `sbbs`
software. Access to the subcategories is determined by checking for
membership in the appropriate group (sbbs-<category-name>).

### Aggressive Encryption
The server has a secret key in the installation that is readable only
by the root user. Aggressive encryption means we encrypt as early as
possible and decrypt as late as possible.

My background is as a UNIX security engineer, and I have attempted to
apply these principles to software development in Clojure.


## License

Copyright 2012 Kyle Isom <coder@kyleisom.net>. 
Distributed under the ISC license.
