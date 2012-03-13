## tum, a simple command to manage a tumblr blog.

This is a command line tool written in scala. The purpose is to let you manage
your Tumblr blog more or less like a git repository. Your posts are markdown
files in a local directory. The name of the file is the slug of the post. The
tags are metadata embedded into the markup. 

    slug.md content:
    =====================================
    # my post title
    bla bla bla...
    bla bla[1]

    [1]: http://...
    <!-- tags:poney,unicorn -->
    =====================================

## Usage

    To publish a post:
        tum [-u <user>] [--force] <file>
    To list posts:
        tum [-u <user>] -l [-r [<type>[ <status>]]]
    To print diff between local and tumblr
        tum [-u <user>] -d
    To get remote post into local file:
        tum [-u <user>] -g <file>
    To commit a local change to a remote post:
        tum [-u <user>] -c <file>
    To print this help:
        tum -h

## Publishing workflow

I use it this way:

 1.  write a new post in file post.md
 2.  then I push it as draft: `$ tum -u me post.md`
 3.  then I review, optionally correct it and publish it via tumblr web ui.
 4.  then I resync local with online version: `$ tum -u me -g post.md`

NB: this project requires some more hacking to be really useful. Contact me if
you are interested in it.

## Under the hood

This project uses [dispatch][dispatch], [configgy][configgy] (a custom version),
[scalatest][scalatest] and [proguard][proguard] in order to have a lightweight 
command tool.

[dispatch]: http://dispatch.databinder.net/Dispatch.html
[configgy]: https://github.com/robey/configgy
[scalatest]: http://www.scalatest.org/
[proguard]: http://proguard.sourceforge.net/
