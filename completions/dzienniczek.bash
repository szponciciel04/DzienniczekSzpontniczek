_dzienniczek() {
  local current commands
  current="${COMP_WORDS[COMP_CWORD]}"
  commands="login logout profile account dashboard accounts periods heartbeat addressbook announcements completed-lessons duties exams grades homework kindergarten-hours kindergarten-teachers lucky-number meal-menu meetings notes planned-lessons presence messages message schedule schedule-extra school-info teachers timeslots trips events vacations push credential subjects users classrooms notices capabilities config version help"
  COMPREPLY=( $(compgen -W "$commands" -- "$current") )
}
complete -F _dzienniczek dzienniczek
