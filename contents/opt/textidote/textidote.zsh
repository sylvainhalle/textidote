#compdef textidote
# ----------------------------------------------------------------------
# Zsh completions. To enable in your Zsh shell, add the line
#
#     source /opt/textidote/textidote.zsh
#
# in your .zshrc file.
# ----------------------------------------------------------------------
_textidote_complete()
{
	_arguments '--check[Check grammar]:lang:->lang' '--clean[Clear markup]' '--dict[Use dictionary]:filename:_files' '--help[Show command line usage]' '--ignore[Ignore rules]' '--languagemodel[Use n-grams]' '--map[Produce map file]:filename:_files' '--version[Show version]' '--no-color[No ANSI color]' '--no-config[Ignore config file]' '--quiet[No messages]' '--read-all[Read all file]' '--remove[Remove environments]' '--remove-macros[Remove macros]' '--replace[Apply replacements]:filename:_files' '--type[Set input type]:type:->type' '--version[Show version]' '--output[Select output format]:format:->format'
	case "$state" in
	lang)
		_values -s ' ' 'lang' de de_AT de_CH en en_CA en_UK es fr nl pt
		;;
	format)
		_values -s ' ' 'format' json html plain singleline
		;;
	type)
		_values -s ' ' 'type' md tex
		;;
	texfiles)
		local -a tex_files
		tex_files=(*.tex)
		_multi_parts / tex_files
		;;
	txtfiles)
		local -a dict_files
		dict_files=(*.txt)
		_multi_parts / dict_files
		;;
	*)
		local -a tex_files
		tex_files=(*.tex)
		_multi_parts / tex_files
		;;
	esac
}

compdef _textidote_complete textidote

# :mode=shellscript:wrap=none: