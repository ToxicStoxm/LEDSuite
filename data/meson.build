i18n.merge_file(
  type        : 'desktop',
  input       : configure_file(
    input         : '@0@.desktop.in.in'.format(project_id),
    output        : '@BASENAME@',
    configuration : metadata_conf
  ),
  output      : '@BASENAME@',
  po_dir      : meson.project_source_root() / 'po',
  install     : true,
  install_dir : '@0@/applications'.format(get_option('prefix') / get_option('datadir'))
)

subdir('icons')

