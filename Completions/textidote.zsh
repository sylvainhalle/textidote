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
	_arguments '--check[Check grammar]:lang:->lang' '--clean[Clear markup]' '--dict[Use dictionary]:filename:_files' '--help[Show command line usage]' '--ignore[Ignore rules]' '--languagemodel [Use n-grams data from dir]' '--map[Output correspondence map to file]' '--no-config[Ignore config file if any]' '--output [Output method is]:method:->method' '--map[Produce map file]:filename:_files' '--no-color[No ANSI color]' '--quiet[No messages]' '--read-all[Read all file]' '--remove[Remove LaTeX environments envs]' '--remove-macros[Remove LaTeX macros macs]' '--replace[Apply replacements]:filename:_files' '--type[Input is of type]:type:->type'
	case "$state" in
	lang)
		_values -s ' ' 'lang' de de_AT de_CH de_DE en en_CA en_UK en_US es fr nl pt
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
	type)
		_values -s ' ' 'type' tex md
		;;
	method)
		_values -s ' ' 'method' plain html json singleline
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