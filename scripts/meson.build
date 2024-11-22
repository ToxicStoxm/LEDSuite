run_command(
	find_program('copy_files.sh'),
	check	: true
)

script_configuration = {
	'SRC_FILE'	            : 'build/libs/@0@'.format(jar_filename),
	'DST_FILE'	            : final_jar_path,
	'TRANSLATION_DIRECTORY' : get_option('prefix') / get_option('localedir')
}

meson.add_install_script(
  configure_file(
	  input		      : 'install.sh.in'.format(scripts_dir),
	  output		    : '@BASENAME@',
	  configuration	: script_configuration
  )
)

install_data(
	configure_file(
	  input         : '@0@/LEDSuite.sh.in'.format(scripts_dir),
	  output        : '@BASENAME@',
	  configuration	: script_configuration
  ),
	install_dir	  : get_option('bindir'),
	install_mode	: 'rwxr-xr-x',
	rename		    : [meson.project_name()]
)
