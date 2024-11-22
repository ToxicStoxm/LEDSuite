appdata = i18n.merge_file(
  type        : 'xml',
  input       : configure_file(
    input         : '@0@.metainfo.xml.in.in'.format(project_id),
    output        : '@BASENAME@',
    configuration : metadata_conf
  ),
  output      : '@BASENAME@',
  po_dir      : meson.project_source_root() / 'po',
  install     : true,
  install_dir : '@0@/metainfo'.format(get_option('prefix') / get_option('datadir'))
)
