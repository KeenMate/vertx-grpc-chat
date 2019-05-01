#!/bin/bash

workingdir=$(pwd)
outdir=${workingdir}/grpc

toRm=${outdir}/*
# clear old
rm $toRm

# generate code
cd proto

protoc *.proto --js_out=import_style=commonjs,binary:${outdir} --grpc-web_out=import_style=commonjs+dts,mode=grpcwebtext:${outdir}

cd $workingdir

# move generated code to appropiate dest
#tmpdir=${outdir}/proto/
#mv $(ls $tmpdir) grpc && rmdir $tmpdir
