#!/bin/sh

test_home=$HOME/Documents/Assignments/cs455/HW1-PC/cs455/a1

for i in `cat $test_home/machine_list`
do
echo 'logging into '${i}
gnome-terminal -x bash -c "ssh -t ${i} 'cd ${test_home}; java cs455.overlay.node.MessagingNode lincoln 53421;bash;'"  &
done
