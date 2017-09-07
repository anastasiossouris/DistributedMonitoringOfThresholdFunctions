#PBS -q tuc
#PBS -oe
#PBS -l nodes=5:ppn=1

# where is the file holding the names of the ordinary nodes
export ordinaryNodeNamesFilename=/storage/tuclocal/asouris/nodes

# the port where the rmi registry of the coordinator will listen
export coordinatorRmiRegistryPort=5000

# the name by which the coordinator binds to the rmi registry
export coordinatorRmiRegistryBindName=Coordinator

# the port where the rmi registry of the ordinary nodes will listen
export ordinaryNodesRmiRegistryPort=5500

# where the properties xml filename is
export propertiesXMLFilename=/storage/tuclocal/asouris/properties.xml

# the home of the project under which bin is appended
export monitoringProjectHome=/storage/tuclocal/asouris/MonitoringThresholdFunctions

# since we will start a ordinary node in each of the nodes we take from qsub
# we copy the PBS_NODEFILE to ordinaryNodeNamesFilename
cat $PBS_NODEFILE >> $ordinaryNodeNamesFilename


coordinatorOk=0


# The hosts we receive by qsub are in the PBS_NODEFILE
while read node
do
	# the first node is the one where the coordinator will run
	if [ $coordinatorOk -eq 0 ]; then
		set coordinatorHost=$node

		# run the rmi registry in the node at the port coordinatorRmiRegistryPort
		pbsdsh -h $node rmiregistry $coordinatorRmiRegistryPort

		# start the coordinator there
		pbsdsh -h $node java -Dproperties.xml.filename=$propertiesXMLFilename -Dcoordinator.rmi.registry.host=$node -Dcoordinator.rmi.registry.port=$coordinatorRmiRegistryPort \
			-Dcoordinator.rmi.registry.bind.name=$coordinatorRmiRegistryBindName -Dordinarynode.names.filename=$ordinaryNodeNamesFilename \
			$monitoringProjectHome/bin/gr/tuc/softnet/monitoring/protocol/coordinator/CoordinatorMain

		coordinatorOk=$[$coordinatorOk + 1]

		# wait a while for the coordinator to start...
		sleep 10
	fi

	# now for the ordinary node...
	# run the rmi registry in the node at the port ordinaryNodesRmiRegistryPort
	pbsdsh -h $node rmiregistry $ordinaryNodesRmiRegistryPort
	# start the ordinary node there
	pbsdsh -h $node java -Dproperties.xml.filename=$propertiesXMLFilename -Dcoordinator.rmi.registry.host=$coordinatorHost -Dcoordinator.rmi.registry.port=$coordinatorRmiRegistryPort \
		-Dcoordinator.rmi.registry.bind.name=$coordinatorRmiRegistryBindName -Dordinarynode.rmi.registry.host=$node -Dordinarynode.rmi.registry.port=$ordinaryNodesRmiRegistryPort \
		-Dordinarynode.rmi.registry.bind.name=$node -Dordinarynode.name=$node $monitoringProjectHome/bin/gr/tuc/softnet/monitoring/protocol/ordinarynode/OrdinaryNodeMain
done < $PBS_NODEFILE
