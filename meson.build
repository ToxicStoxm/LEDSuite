project(
  'LEDSuite',
  'c',
  version         : '1.0.0-rc1',
  meson_version   : '>= 1.3.0',
  license         : 'GPL-3.0-or-later'
)

developer_name = 'ToxicStoxm'
developer_id = 'com.@0@'.format(developer_name.to_lower())
project_id = '@0@.@1@'.format(developer_id, meson.project_name())
scripts_dir = '@0@/scripts'.format(meson.project_source_root())
jar_filename = '@0@-fat-@1@.jar'.format(meson.project_name(),  meson.project_version())
final_jar_path = '@0@/@1@.jar'.format(get_option('prefix') / get_option('bindir'), meson.project_name())

i18n = import('i18n')
gnome = import('gnome')

dependency('gtk4', version: '>= 4.16')
dependency('libadwaita-1', version: '>= 1.6')
dependency('glib-2.0', version: '>= 2.66')
dependency('gio-2.0', version: '>= 2.82')

find_program('glib-compile-resources', required: true)

gradle = find_program(
	'gradle',
	dirs	    : ['/usr/lib/sdk/openjdk/gradle/bin'],
	required	: false
)

if not gradle.found()
	gradle = find_program('gradlew')
endif

cp = find_program('cp')

env = environment()

if get_option('javadir') != '' 
  env.set('JAVA_HOME', get_option('javadir'))
endif

metadata_conf = {
	'ID'	            : project_id,
	'NAME'	          : meson.project_name(),
	'DEVELOPER_ID'    : developer_id,
	'DEVELOPER_NAME'  : developer_name,
	'DESKTOP_FILE'    : '@0@.desktop'.format(project_id),
	'VERSION'         : meson.project_version(),
	'COMMAND'         : meson.project_name()
}

subdir('data')
subdir('src/main/resources')

custom_target(
	'compile-project',
	depends             : [appdata],
	input               : [configure_file(
	                        input         : 'build.gradle',
	                        output        : 'build.gradle',
	                        configuration : metadata_conf,
	                        install       : false
	                       )],
	build_by_default	  : true, # Always recompile the project
	build_always_stale	: true,
	console			        : true,
	command			        : [gradle.full_path(), 'jar'],
	env			            : env,
	install_dir	        : [get_option('libdir'), get_option('bindir')],
	output			        : [jar_filename]
)

subdir('po')
subdir('scripts')

gnome.post_install(
  gtk_update_icon_cache: true,
  glib_compile_schemas: false,
  update_desktop_database: true,
)

