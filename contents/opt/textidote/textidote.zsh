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
	_arguments '--check[Check grammar]:lang:->lang' '--detex[Clear markup]' '--dict[Use dictionary]:filename:_files' '--help[Show command line usage]' '--html[Produce HTML output]' '--map[Produce map file]:filename:_files' '--no-color[No ANSI color]' '--quiet[No messages]' '--read-all[Read all file]' '--replace[Apply replacements]:filename:_files'
	case "$state" in
	lang)
		_values -s ' ' 'lang' de de_AT de_CH en en_CA en_UK es fr nl pt
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