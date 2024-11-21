project('LEDSuite', 'c', version: '1.0.0-non-final', meson_version: '>= 1.3.0', license: 'GPL-3.0-or-later')

project_id = 'com.toxicstoxm.LEDSuite'

i18n = import('i18n')

dependency('gtk4', version: '>= 4.16')
dependency('libadwaita-1', version: '>= 1.6')
dependency('glib-2.0', version: '>= 2.66')
dependency('gio-2.0', version: '>= 2.82')

find_program('glib-compile-resources', required: true)

scripts_dir = '@0@/scripts'.format(meson.current_source_dir())

run_command(
	find_program(
		'copy_files.sh',
		dirs	: [scripts_dir]
	),
	check	: true,
)

subdir('po')

jar_filename = '@0@-fat-@1@.jar'.format(meson.project_name(),  meson.project_version())

gradle = find_program(
	'gradle',
	dirs	: ['/usr/lib/sdk/openjdk/gradle/bin']
)

if not gradle.found()
	gradle = find_program(
		'gradlew',
		required	: false
	)
endif

custom_target(
	'Java compile using gradle',
	build_by_default	: true, # Always recompile the project
	build_always_stale	: true,
	console			: true,
	command			: [gradle.full_path(), 'jar'],
	install_dir		: [get_option('libdir'), get_option('bindir')],
	output			: [jar_filename]
)

conf = {
	'SRC_FILE'	: 'build/libs/@0@'.format(jar_filename),
	'DST_FILE'	: '@0@/@1@'.format(get_option('prefix') / get_option('bindir'), 'LEDSuite.jar')
}

install_script = configure_file(
	input		: '@0@/install.sh.in'.format(scripts_dir),
	output		: '@BASENAME@',
	configuration	: conf
)

meson.add_install_script(install_script)

run_script = configure_file(
	input		: '@0@/LEDSuite.sh.in'.format(scripts_dir),
	output		: '@BASENAME@',
	configuration	: conf
)

install_data(
	run_script,
	install_dir	: get_option('bindir'),
	install_mode	: 'rwxr-xr-x',
	rename		: ['LEDSuite']
)

