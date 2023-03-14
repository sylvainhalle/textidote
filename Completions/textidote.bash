# Bash completion script for TeXtidote
# Based on this template:
# https://debian-administration.org/article/317/An_introduction_to_bash_completion_part_2

beginswith() 
{ 
	case $2 in 
		"$1"*) true;; 
		*) false;; 
	esac; 
}

# Found here: https://superuser.com/a/564776
completeFiles()
{
	local IFS=$'\n'
    local LASTCHAR=' '
    COMPREPLY=($(compgen -o plusdirs -f -- "${COMP_WORDS[COMP_CWORD]}"))
    if [ ${#COMPREPLY[@]} = 1 ]; then
        [ -d "$COMPREPLY" ] && LASTCHAR=/
        COMPREPLY=$(printf %q%s "$COMPREPLY" "$LASTCHAR")
    else
        for ((i=0; i < ${#COMPREPLY[@]}; i++)); do
            [ -d "${COMPREPLY[$i]}" ] && COMPREPLY[$i]=${COMPREPLY[$i]}/
        done
    fi
}

_textidote()
{
	local cur prev opts base
	COMPREPLY=()
	cur="${COMP_WORDS[COMP_CWORD]}"
    prev="${COMP_WORDS[COMP_CWORD-1]}"
    
    #
    #  The basic options we'll complete.
    #
    opts="--check --ci --clean --dict --encoding --firstlang --help --ignore --languagemodel --map --name --no-color --no-config --output --quiet --read-all --remove --remove --replace --type --version"
    
    #
    #  Complete the arguments to some of the basic commands.
    #
    case "${prev}" in
        --check)
            local langs="ar de de_AT de_CH de_DE en en_CA en_UK en_US es fr nl pt pl"
            COMPREPLY=( $(compgen -W "${langs}" -- ${cur}) )
            return 0
            ;;
        --dict)
            completeFiles
            return 0
            ;;
        --encoding)
            local encodings="ASCII cp437 cp1252 UTF8"
            COMPREPLY=( $(compgen -W "${encodings}" -- ${cur}) )
            return 0
            ;;
        --map)
            completeFiles
            return 0
            ;;
        --output)
            local methods="html json plain singleline"
            COMPREPLY=( $(compgen -W "${methods}" -- ${cur}) )
            return 0
            ;;
        --replace)
            completeFiles
            return 0
            ;;
        --type)
            local types="tex md"
            COMPREPLY=( $(compgen -W "${types}" -- ${cur}) )
            return 0
            ;;
        *)
            ;;
    esac
    
    # If we are writing an option, complete from option list
    if beginswith "-" $cur; then
    	COMPREPLY=($(compgen -W "${opts}" -- ${cur}))
    	return 0
    fi

   # Reply with list of TeX files
   completeFiles
   return 0
}

_textidote_zsh()
{
	compadd --check --ci --clean --dict --encoding --firstlang --help --ignore --languagemodel --map --name --no-color --no-config --output --quiet --read-all --remove --remove --replace --type --version
}

# Register the goto completions.
if [ -n "${BASH_VERSION}" ]; then
  if ! [[ $(uname -s) =~ Darwin* ]]; then
    complete -o filenames -F _textidote textidote
  else
    complete -F _textidote textidote
  fi
elif [ -n "${ZSH_VERSION}" ]; then
  compdef_textidote_zsh textidote
else
  echo "Unsupported shell."
  exit 1
fi

# :mode=bash: