<?xml version="1.0" encoding="UTF-8"?>
<mspec:mspec xmlns:mspec="http://www.eclipse.org/buckminster/MetaData-1.0" 
	installLocation="_target" 
	name="buckminster.mspec" url="build.cquery">
	
	<mspec:mspecNode namePattern="^EMFStore(?:\..+)?$" materializer="workspace"/>
	<mspec:mspecNode namePattern="^org\.eclipse\.emf\.emfstore(?:\..+)?$" materializer="workspace"/>
	<mspec:mspecNode namePattern="^ECP(?:\..+)?$" materializer="workspace"/>
	<mspec:mspecNode namePattern="^org\.eclipse\.emf\.ecp(?:\..+)?$" materializer="workspace"/>
</mspec:mspec>