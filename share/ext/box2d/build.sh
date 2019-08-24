#! /usr/bin/env bash

readonly BASE_URL=https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/box2d
readonly FILE_URL=Box2D_v2.2.1.zip
readonly PRODUCT=box2d
readonly VERSION=2.2.1

export CONF_TARGET=$1

. ../common.sh

function cmi_unpack() {
    unzip -q ../../download/$FILE_URL

    local tmpdir=`pwd`

    pushd Box2D_v${VERSION}/Box2D

    # Convert line endings to unix style (to make the patch work)
    find . -type f -name "*.*" -exec dos2unix {} \;

    # copy our extra files
    cp -v -r ${tmpdir}/../extra/Box2D/ .

    popd
}

function cmi_configure() {
    pushd Box2D_v${VERSION}/Box2D

    cmake -DCMAKE_BUILD_TYPE=Release -DBOX2D_BUILD_STATIC=ON -DBOX2D_VERSION="${VERSION}" -DBOX2D_INSTALL=OFF -DBOX2D_BUILD_EXAMPLES=OFF

    popd
}

LIB_SUFFIX=
case $1 in
     *win32)
        LIB_SUFFIX=".lib"
        ;;
     *)
        LIB_SUFFIX=".a"
        ;;
esac

function cmi_make() {
    set -e
    pushd Box2D_v${VERSION}/Box2D
    echo cmi_make
    pwd
    make -j8 VERBOSE=1

    export MACOSX_DEPLOYMENT_TARGET=${OSX_MIN_SDK_VERSION}

    # "install"
    mkdir -p $PREFIX/bin
    mkdir -p $PREFIX/lib/$CONF_TARGET
    mkdir -p $PREFIX/include/Box2D
    mkdir -p $PREFIX/share

    find . -iname "*${LIB_SUFFIX}" -print0 | xargs -0 -I {} cp -v {} $PREFIX/lib/$CONF_TARGET
    find . -name "*.h" -print0 | cpio -pmd0 $PREFIX/include/Box2D

    popd
    set +e
}


download
cmi $1