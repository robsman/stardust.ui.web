#!/bin/bash
#
# $Id$
# (C) 2000-2011 SunGard CSA LLC
# 
# script to start ant for carnot in a *nix environment

#
# if an environment variable is not set take a reasonable guess
#

function write_message()
{
        local message=$1
        local exit_code=$2

        # print message to stderr
        echo -e "$message" >&2

        [ ! -z "$exit_code" ] && exit $exit_code
}


[ -z "$CM_HOME" ] && {
    write_message "CM_HOME is not defined" 1
}

echo "Starting generic CM antit.sh..."

$CM_HOME/bin/antit17.sh $*

